package com.equipo10.lince.model

/** Estado completo de la pantalla "Disponibilidad". */
data class DisponibilidadUiState(
    val cargando: Boolean = true,
    val dias: List<DiaDisponibilidad> = emptyList(),
    val bloqueados: Int = 0,
    val mensaje: String = "Puedes bloquear días con más de una semana de anticipación.",
    val mensajeEsAlerta: Boolean = false,
    val error: String? = null
)
