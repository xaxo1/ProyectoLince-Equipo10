package com.equipo10.lince.model

/** Estado completo de la pantalla "Servicios del día" (Inicio). */
data class InicioUiState(
    val cargando: Boolean = true,
    val nombre: String = "",
    val rol: String = "",
    val fechaHoy: String = "",
    val serviciosHoy: List<Servicio> = emptyList(),
    val proximo: Servicio? = null,
    val error: String? = null
)
