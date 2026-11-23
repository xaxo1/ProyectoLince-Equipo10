package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.ServiceLocator
import com.equipo10.lince.data.UbicacionDispositivo
import com.equipo10.lince.model.DetalleUiState
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.Rol
import com.equipo10.lince.model.TipoHito
import com.equipo10.lince.model.Ubicacion
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.util.Geocerca
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del "Detalle del servicio".
 * Check-in: lee el GPS -> el teléfono calcula la distancia al hotel (Haversine) -> se envía al backend,
 * que vuelve a validar y responde VERIFICADA, FUERA_DE_ZONA, ATRASADA o GPS_IMPRECISO.
 */
class DetalleServicioViewModel(
    private val repositorio: RepositorioLince = ServiceLocator.repositorio,
    private val leerUbicacion: suspend () -> Result<Ubicacion> = { UbicacionDispositivo.obtenerUbicacion() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    private var servicioId: Int = -1

    fun cargar(idServicio: Int) {
        servicioId = idServicio
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            val reglas = repositorio.reglas()
            repositorio.servicio(idServicio)
                .onSuccess { servicio ->
                    val geocerca = repositorio.geocercaDe(servicio.puntoPickup).getOrNull()
                    val registros = repositorio.registrosDelServicio(idServicio).getOrDefault(emptyList())
                    val incidencias = repositorio.incidencias(idServicio).getOrDefault(emptyList())
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            servicio = servicio,
                            esTransportista = repositorio.usuarioActual?.rol == Rol.TRANSPORTISTA,
                            geocerca = geocerca,
                            reglas = reglas,
                            ultimoRegistro = registros.lastOrNull { r -> r.tipo != TipoHito.POSICION },
                            incidencias = incidencias,
                            mensaje = mensajeSegunEstado(servicio.estado, servicio.fecha, servicio.horaPickup, reglas.minutosAntes),
                            mensajeEsAlerta = servicio.estado == EstadoServicio.CANCELADO || servicio.conAtraso
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudo cargar el servicio") } }
        }
    }

    /** CONFIRMADO -> check-in -> EN_CURSO -> check-out -> FINALIZADO. */
    fun avanzarEstado() {
        val actual = _uiState.value
        val servicio = actual.servicio ?: return
        if (actual.procesando) return
        val tipo = when (servicio.estado) {
            EstadoServicio.CONFIRMADO -> TipoHito.CHECK_IN
            EstadoServicio.EN_CURSO -> TipoHito.CHECK_OUT
            else -> return
        }
        _uiState.update { it.copy(procesando = true, mensaje = "Obteniendo ubicación GPS...", mensajeEsAlerta = false) }
        viewModelScope.launch {
            val ubicacion = leerUbicacion().getOrElse { e ->
                _uiState.update { it.copy(procesando = false, mensaje = "Sin ubicación: ${e.message}", mensajeEsAlerta = true) }
                return@launch
            }
            // El teléfono calcula la distancia al hotel para mostrarla (el servidor la vuelve a calcular)
            val distancia = actual.geocerca?.let {
                Geocerca.distanciaMetros(ubicacion.latitud, ubicacion.longitud, it.latitud, it.longitud)
            }
            _uiState.update { it.copy(ubicacionActual = ubicacion, distanciaActual = distancia) }

            repositorio.registrarHito(servicio.id, tipo, ubicacion)
                .onSuccess { registro ->
                    _uiState.update {
                        it.copy(procesando = false, ultimoRegistro = registro, mensaje = registro.mensaje, mensajeEsAlerta = !registro.verificado)
                    }
                    cargar(servicio.id)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(procesando = false, mensaje = e.message ?: "No se pudo registrar", mensajeEsAlerta = true) }
                }
        }
    }

    /** Lee el GPS solo para mostrar distancia y precisión, sin registrar nada. */
    fun medirDistancia() {
        val geocerca = _uiState.value.geocerca ?: return
        viewModelScope.launch {
            leerUbicacion()
                .onSuccess { u ->
                    val d = Geocerca.distanciaMetros(u.latitud, u.longitud, geocerca.latitud, geocerca.longitud)
                    _uiState.update { it.copy(ubicacionActual = u, distanciaActual = d) }
                }
                .onFailure { e -> _uiState.update { it.copy(mensaje = "Sin ubicación: ${e.message}", mensajeEsAlerta = true) } }
        }
    }

    /** Transportista en servicio activo: reporte PUNTUAL de posición para coordinación (no rastreo 24/7). */
    fun reportarPosicion() {
        val servicio = _uiState.value.servicio ?: return
        viewModelScope.launch {
            val ubicacion = leerUbicacion().getOrElse { e ->
                _uiState.update { it.copy(mensaje = "Sin ubicación: ${e.message}", mensajeEsAlerta = true) }
                return@launch
            }
            repositorio.registrarHito(servicio.id, TipoHito.POSICION, ubicacion)
                .onSuccess { _uiState.update { it.copy(mensaje = "Posición enviada a coordinación", mensajeEsAlerta = false) } }
                .onFailure { e -> _uiState.update { it.copy(mensaje = e.message ?: "No se pudo enviar", mensajeEsAlerta = true) } }
        }
    }

    /** Confirmar o rechazar la asignación (queda en el backend propio). */
    fun responderAsignacion(confirmar: Boolean) {
        val servicio = _uiState.value.servicio ?: return
        viewModelScope.launch {
            repositorio.responderAsignacion(servicio.id, confirmar, null)
                .onSuccess { estado ->
                    _uiState.update {
                        it.copy(
                            servicio = servicio.copy(confirmacion = estado),
                            mensaje = if (confirmar) "Asignación confirmada" else "Asignación rechazada",
                            mensajeEsAlerta = !confirmar
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(mensaje = e.message ?: "No se pudo responder", mensajeEsAlerta = true) } }
        }
    }

    // WHEN como expresión: un mensaje distinto para cada estado
    private fun mensajeSegunEstado(estado: EstadoServicio, fecha: String, hora: String, minutosAntes: Int): String = when (estado) {
        EstadoServicio.CONFIRMADO ->
            if (fecha == Formatos.hoy()) "Llega al hotel al menos $minutosAntes min antes del pickup de las $hora y haz check-in dentro de la geocerca."
            else "Podrás hacer check-in el día del servicio (${Formatos.fechaLegible(fecha)})."
        EstadoServicio.EN_CURSO -> "Servicio en curso. Al terminar en el dropoff, registra el check-out."
        EstadoServicio.FINALIZADO -> "Servicio finalizado."
        EstadoServicio.CANCELADO -> "Este servicio está cancelado (estado Lince XX o asignación CX/DL)."
    }
}
