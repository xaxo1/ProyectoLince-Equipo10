package com.equipo10.lince.model

/** Estado completo del "Detalle del servicio" (check-in / check-out con geocerca). */
data class DetalleUiState(
    val cargando: Boolean = true,
    val servicio: Servicio? = null,
    val esTransportista: Boolean = false,
    val geocerca: GeocercaInfo? = null,                   // hotel de pickup: centro + radio
    val reglas: Reglas = Reglas(),
    val ultimoRegistro: RegistroHito? = null,             // respuesta del servidor (distancia, radio, precisión)
    val ubicacionActual: Ubicacion? = null,               // última lectura del GPS
    val distanciaActual: Double? = null,                  // calculada en el teléfono (Haversine)
    val procesando: Boolean = false,
    val mensaje: String = "Cargando servicio...",
    val mensajeEsAlerta: Boolean = false,
    val incidencias: List<Incidencia> = emptyList(),
    val error: String? = null
)
