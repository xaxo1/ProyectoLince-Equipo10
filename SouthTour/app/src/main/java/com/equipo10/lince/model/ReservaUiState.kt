package com.equipo10.lince.model

/** Estado completo de la pantalla "Reserva completa". */
data class ReservaUiState(
    val cargando: Boolean = true,
    val reserva: Reserva? = null,
    val error: String? = null
)
