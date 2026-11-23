package com.equipo10.lince.model

/** Estado completo de la pantalla "Hoy" (servicios del día). */
data class InicioUiState(
    val cargando: Boolean = true,
    val nombre: String = "",
    val rol: String = "",
    val fechaHoy: String = "",
    val serviciosHoy: List<Servicio> = emptyList(),
    val proximo: Servicio? = null,
    val desdeCache: Boolean = false,          // true: sin internet, datos guardados en Room
    val pendientesSincronizar: Int = 0,       // hitos en la cola offline
    val error: String? = null
) {
    /** Propiedades calculadas (colecciones: sumOf, none). */
    val totalPasajeros: Int
        get() = serviciosHoy.filter { it.estado != EstadoServicio.CANCELADO }.sumOf { it.pasajeros }

    val jornadaTerminada: Boolean
        get() = serviciosHoy.none { it.estado == EstadoServicio.CONFIRMADO || it.estado == EstadoServicio.EN_CURSO }
}
