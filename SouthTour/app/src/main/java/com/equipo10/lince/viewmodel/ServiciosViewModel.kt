package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.ServiceLocator
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.FiltroServicios
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.model.ServiciosUiState
import com.equipo10.lince.model.TipoServicio
import com.equipo10.lince.model.agruparPorFecha
import com.equipo10.lince.util.Formatos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel del "Calendario": filtros por estado, tipo y fecha + búsqueda por texto. */
class ServiciosViewModel(
    private val repositorio: RepositorioLince = ServiceLocator.repositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiciosUiState())
    val uiState: StateFlow<ServiciosUiState> = _uiState.asStateFlow()

    private var todos: List<Servicio> = emptyList()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            // Una semana hacia atrás (historial) y un mes hacia adelante
            repositorio.serviciosEntre(Formatos.fechaEnDias(-7), Formatos.fechaEnDias(30)).fold(
                onSuccess = { lista ->
                    todos = lista.servicios
                    aplicarFiltros()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudieron cargar los servicios") }
                }
            )
        }
    }

    fun cambiarFiltro(filtro: FiltroServicios) {
        _uiState.update { it.copy(filtro = filtro) }
        aplicarFiltros()
    }

    /** Filtro por tipo (TRANSFER, GUIDED_SERVICE, EXCURSION). Tocar el mismo tipo lo quita. */
    fun cambiarTipo(tipo: TipoServicio?) {
        _uiState.update { it.copy(filtroTipo = if (it.filtroTipo == tipo) null else tipo) }
        aplicarFiltros()
    }

    /** Día elegido en la cuadrícula del calendario. Tocar el mismo día lo quita. */
    fun seleccionarFecha(fecha: String?) {
        _uiState.update { it.copy(fechaSeleccionada = if (it.fechaSeleccionada == fecha) null else fecha) }
        aplicarFiltros()
    }

    /** Búsqueda en vivo: se vuelve a filtrar cada vez que cambia el texto. */
    fun cambiarBusqueda(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val estado = _uiState.value
        // FUNCIÓN DE ORDEN SUPERIOR: el criterio de filtrado se guarda en una variable
        val criterio: (Servicio) -> Boolean = when (estado.filtro) {
            FiltroServicios.PROXIMOS -> { s -> s.estado == EstadoServicio.CONFIRMADO }
            FiltroServicios.EN_CURSO -> { s -> s.estado == EstadoServicio.EN_CURSO }
            FiltroServicios.FINALIZADOS -> { s -> s.estado == EstadoServicio.FINALIZADO }
            FiltroServicios.CANCELADOS -> { s -> s.estado == EstadoServicio.CANCELADO }
            FiltroServicios.TODOS -> { _ -> true }
        }
        val texto = estado.busqueda.trim()
        val filtrados = todos
            .filter(criterio)
            .filter { estado.filtroTipo == null || it.tipo == estado.filtroTipo }
            .filter { estado.fechaSeleccionada == null || it.fecha == estado.fechaSeleccionada }
            .filter { texto.isEmpty() || it.coincideConBusqueda(texto) }

        // Próximos: del más cercano al más lejano. Historial: del más reciente al más antiguo.
        val ordenados = if (estado.filtro == FiltroServicios.PROXIMOS) filtrados.sortedBy { it.fecha + it.horaPickup }
            else filtrados.sortedByDescending { it.fecha + it.horaPickup }

        _uiState.update {
            it.copy(
                cargando = false,
                porFecha = ordenados.agruparPorFecha(),
                diasConServicio = todos.map { s -> s.fecha }.toSet(),
                cantidad = ordenados.size,
                total = todos.size,
                error = null
            )
        }
    }
}
