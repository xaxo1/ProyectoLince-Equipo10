package com.equipo10.lince.model

/** Estado completo de la pantalla "Resumen" (Clase 6: data class con valores por defecto). */
data class ResumenUiState(
    val cargando: Boolean = true,
    val desplazamientoMes: Int = 0,      // 0 = mes actual, -1 = mes anterior...
    val resumen: ResumenMensual? = null,
    val error: String? = null
)
