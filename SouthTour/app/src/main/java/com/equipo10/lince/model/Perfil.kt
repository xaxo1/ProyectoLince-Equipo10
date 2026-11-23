package com.equipo10.lince.model

/**
 * Resumen del mes (pantalla "Resumen"): mis servicios según la Lince API + mis hitos del backend,
 * más el resumen global de Southbound (endpoint /api/v1/resumen).
 */
data class ResumenMensual(
    val mes: String,                    // "Noviembre 2026"
    val esMesActual: Boolean,
    val misServicios: Int,
    val completados: Int,               // servicios con check-out
    val cancelados: Int,                // XX o asignación CX/DL
    val puntualidad: Int?,              // % de check-in verificados; null si no hubo check-in
    val porTipo: Map<TipoServicio, Int>,
    val totalSouthbound: Int,           // servicios de toda la empresa en el mes
    val porEstadoSouthbound: Map<String, Int>,
    val detalle: List<Servicio>
)

/** Datos que se muestran en la pantalla de Perfil. */
data class Perfil(
    val usuario: Usuario,
    val empresa: String = "Southbound",
    val serviciosDelMes: Int,
    val hitosRegistrados: Int,
    val pendientesSincronizar: Int
)
