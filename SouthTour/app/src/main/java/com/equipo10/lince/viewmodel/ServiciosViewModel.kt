package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.FiltroServicios
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.model.ServiciosUiState
import com.equipo10.lince.model.agruparPorFecha
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel del "Calendario de servicios". */
class ServiciosViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ServiciosUiState())
    val uiState: StateFlow<ServiciosUiState> = _uiState.asStateFlow()

    private var todos: List<Servicio> = emptyList()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            // Result.fold: un camino para el éxito y otro para el fallo
            RepositorioLince.obtenerServicios().fold(
                onSuccess = { lista ->
                    todos = lista
                    aplicarFiltro(_uiState.value.filtro)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudieron cargar los servicios") }
                }
            )
        }
    }

    fun cambiarFiltro(filtro: FiltroServicios) {
        aplicarFiltro(filtro)
    }

    private fun aplicarFiltro(filtro: FiltroServicios) {
        // FUNCIÓN DE ORDEN SUPERIOR: el criterio de filtrado se guarda en una variable
        val criterio: (Servicio) -> Boolean = when (filtro) {
            FiltroServicios.PROXIMOS -> { s -> s.estado == EstadoServicio.CONFIRMADO }
            FiltroServicios.EN_CURSO -> { s -> s.estado == EstadoServicio.EN_CURSO }
            FiltroServicios.FINALIZADOS -> { s -> s.estado == EstadoServicio.FINALIZADO }
            FiltroServicios.CANCELADOS -> { s -> s.estado == EstadoServicio.CANCELADO }
            FiltroServicios.TODOS -> { _ -> true }
        }

        val filtrados = todos.filter(criterio)
        // Próximos: del más cercano al más lejano. Historial: del más reciente al más antiguo.
        val ordenados = if (filtro == FiltroServicios.PROXIMOS) {
            filtrados.sortedBy { it.fecha + it.horaPickup }
        } else {
            filtrados.sortedByDescending { it.fecha + it.horaPickup }
        }

        _uiState.update {
            it.copy(
                cargando = false,
                porFecha = ordenados.agruparPorFecha(),
                filtro = filtro,
                cantidad = ordenados.size,
                total = todos.size,
                error = null
            )
        }
    }
}
