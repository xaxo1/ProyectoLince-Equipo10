package com.equipo10.lince.model

/** Estado completo de la pantalla de Perfil (incluye el resumen mensual). */
data class PerfilUiState(
    val cargando: Boolean = true,
    val perfil: Perfil? = null,
    val error: String? = null
)
