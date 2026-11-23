package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.ServiceLocator
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.InicioUiState
import com.equipo10.lince.util.Formatos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel de "Hoy": servicios del día (Lince API) con su estado según los hitos del backend. */
class InicioViewModel(
    private val repositorio: RepositorioLince = ServiceLocator.repositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            val usuario = repositorio.usuarioActual
            if (usuario == null) {
                _uiState.update { it.copy(cargando = false, error = "No hay una sesión iniciada") }
                return@launch
            }
            val hoy = Formatos.hoy()
            // Una sola consulta: hoy + los próximos 30 días (para mostrar el próximo servicio)
            repositorio.serviciosEntre(hoy, Formatos.fechaEnDias(30))
                .onSuccess { lista ->
                    val deHoy = lista.servicios.filter { it.fecha == hoy }
                    val proximo = lista.servicios.firstOrNull { it.fecha > hoy && it.estado != EstadoServicio.CANCELADO }
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            nombre = usuario.nombre,
                            rol = usuario.rol.etiqueta,
                            fechaHoy = Formatos.fechaLegible(hoy),
                            serviciosHoy = deHoy,
                            proximo = proximo,
                            desdeCache = lista.desdeCache,
                            pendientesSincronizar = repositorio.cantidadPendientes()
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudieron cargar los servicios") } }
        }
    }
}
