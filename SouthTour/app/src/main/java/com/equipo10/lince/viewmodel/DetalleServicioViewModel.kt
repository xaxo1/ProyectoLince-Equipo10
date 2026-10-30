package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.model.DetalleUiState
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.util.Formatos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del "Detalle del servicio".
 * Aplica la Clase 6: avanzarEstado() y reiniciar() actualizan el StateFlow con update { copy() }.
 */
class DetalleServicioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    fun cargar(idServicio: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            RepositorioLince.obtenerServicio(idServicio)
                .onSuccess { servicio ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            servicio = servicio,
                            mensaje = mensajeSegunEstado(servicio),
                            mensajeEsAlerta = servicio.conAtraso || servicio.estado == EstadoServicio.CANCELADO
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudo cargar el servicio") }
                }
        }
    }

    /** CONFIRMADO -> EN_CURSO (check-in) -> FINALIZADO (check-out). */
    fun avanzarEstado() {
        val actual = _uiState.value
        val servicio = actual.servicio ?: return
        if (actual.procesando) return

        _uiState.update { it.copy(procesando = true) }
        viewModelScope.launch {
            RepositorioLince.avanzarEstado(servicio.id)
                .onSuccess { actualizado ->
                    _uiState.update {
                        it.copy(
                            procesando = false,
                            servicio = actualizado,
                            mensaje = mensajeTrasAvanzar(actualizado),
                            mensajeEsAlerta = actualizado.estado == EstadoServicio.EN_CURSO && actualizado.conAtraso
                        )
                    }
                }
                .onFailure { e ->
                    // La regla de negocio no lo permitió: se muestra el motivo en color Alerta
                    _uiState.update {
                        it.copy(procesando = false, mensaje = e.message ?: "No se pudo actualizar", mensajeEsAlerta = true)
                    }
                }
        }
    }

    /**
     * Vuelve el servicio al estado en que lo asignó operaciones (ejemplo de reiniciar() de la Clase 6).
     * NO se muestra en la interfaz: el requisito de trazabilidad (evidencia Clase 1) dice que un
     * check-in o check-out no se puede modificar después de registrado.
     */
    fun reiniciar() {
        val servicio = _uiState.value.servicio ?: return
        _uiState.update { it.copy(procesando = true) }
        viewModelScope.launch {
            RepositorioLince.reiniciarServicio(servicio.id)
                .onSuccess { original ->
                    _uiState.update {
                        it.copy(
                            procesando = false,
                            servicio = original,
                            mensaje = "Servicio reiniciado: ${original.estado.etiqueta}",
                            mensajeEsAlerta = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(procesando = false, mensaje = e.message ?: "No se pudo reiniciar", mensajeEsAlerta = true) }
                }
        }
    }

    // WHEN como expresión: un mensaje distinto para cada estado
    private fun mensajeSegunEstado(servicio: Servicio): String = when (servicio.estado) {
        EstadoServicio.CONFIRMADO ->
            if (servicio.fecha == Formatos.hoy()) "Servicio listo para iniciar. Recuerda llegar 15 min antes del pickup."
            else "Podrás iniciar este servicio el día ${Formatos.fechaLegible(servicio.fecha)}."
        EstadoServicio.EN_CURSO -> "Servicio en curso desde las ${servicio.horaCheckIn ?: "--:--"}."
        EstadoServicio.FINALIZADO -> "Servicio finalizado."
        EstadoServicio.CANCELADO -> "Este servicio fue cancelado por operaciones."
    }

    private fun mensajeTrasAvanzar(servicio: Servicio): String = when (servicio.estado) {
        EstadoServicio.EN_CURSO ->
            if (servicio.conAtraso) "Check-in a las ${servicio.horaCheckIn} con ATRASO: se notificará a operaciones."
            else "Check-in a las ${servicio.horaCheckIn}, dentro del horario. Estado: En curso."
        EstadoServicio.FINALIZADO -> "Check-out a las ${servicio.horaCheckOut}. Servicio finalizado."
        else -> "Estado actualizado a ${servicio.estado.etiqueta}"
    }
}
