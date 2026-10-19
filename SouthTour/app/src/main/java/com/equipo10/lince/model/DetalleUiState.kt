package com.equipo10.lince.model

/** Estado completo del "Detalle del servicio" (incluye check-in / check-out). */
data class DetalleUiState(
    val cargando: Boolean = true,
    val servicio: Servicio? = null,
    val procesando: Boolean = false,                      // mientras se registra el check-in/out
    val mensaje: String = "Cargando servicio...",         // mensaje para el usuario (como en la Clase 6)
    val mensajeEsAlerta: Boolean = false,                 // true -> se pinta en color Alerta
    val error: String? = null                             // error al cargar
)
