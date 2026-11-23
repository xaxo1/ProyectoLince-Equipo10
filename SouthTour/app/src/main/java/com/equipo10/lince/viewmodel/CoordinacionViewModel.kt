package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.ServiceLocator
import com.equipo10.lince.model.GeocercaInfo
import com.equipo10.lince.model.HitoPanel
import com.equipo10.lince.model.Incidencia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado del panel de coordinación. */
data class CoordinacionUiState(
    val cargando: Boolean = true,
    val hitos: List<HitoPanel> = emptyList(),
    val posiciones: List<HitoPanel> = emptyList(),
    val incidencias: List<Incidencia> = emptyList(),
    val geocercas: List<GeocercaInfo> = emptyList(),
    val mensaje: String? = null,
    val error: String? = null
) {
    val verificados: Int get() = hitos.count { it.tipo == "CHECK_IN" && it.resultado == "VERIFICADA" }
    val rechazados: Int get() = hitos.count { it.tipo == "CHECK_IN" && it.resultado != "VERIFICADA" }
}

/** ViewModel del panel operativo: hitos del día, últimas posiciones, incidencias y radios de geocerca. */
class CoordinacionViewModel(
    private val repositorio: RepositorioLince = ServiceLocator.repositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoordinacionUiState())
    val uiState: StateFlow<CoordinacionUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            val hitos = repositorio.panelDelDia()
            hitos.onFailure { e ->
                _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudo cargar el panel") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    cargando = false,
                    hitos = hitos.getOrDefault(emptyList()),
                    posiciones = repositorio.ultimasPosiciones().getOrDefault(emptyList()),
                    incidencias = repositorio.todasLasIncidencias().getOrDefault(emptyList()),
                    geocercas = repositorio.geocercas().getOrDefault(emptyList())
                )
            }
        }
    }

    /** Coordinación ajusta el radio de la geocerca de un hotel (ej. de 150 a 100 m). */
    fun cambiarRadio(geocerca: GeocercaInfo, radio: Int) {
        viewModelScope.launch {
            repositorio.cambiarRadio(geocerca, radio)
                .onSuccess { nueva ->
                    _uiState.update { estado ->
                        estado.copy(
                            geocercas = estado.geocercas.map { if (it.id == nueva.id) nueva else it },
                            mensaje = "Radio de ${nueva.nombre}: ${nueva.radioMetros} m"
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(mensaje = e.message ?: "No se pudo cambiar el radio") } }
        }
    }
}
