package com.equipo10.lince.model

/** Estado completo de la pantalla "Hoy" (servicios del día). */
data class InicioUiState(
    val cargando: Boolean = true,
    val nombre: String = "",
    val rol: String = "",
    val fechaHoy: String = "",
    val serviciosHoy: List<Servicio> = emptyList(),
    val proximo: Servicio? = null,
    val error: String? = null
) {
    /** Propiedades calculadas (colecciones: sumOf, count, none). */
    val totalPasajeros: Int
        get() = serviciosHoy.filter { it.estado != EstadoServicio.CANCELADO }.sumOf { it.pasajeros }

    val jornadaTerminada: Boolean
        get() = serviciosHoy.none { it.estado == EstadoServicio.CONFIRMADO || it.estado == EstadoServicio.EN_CURSO }
}
