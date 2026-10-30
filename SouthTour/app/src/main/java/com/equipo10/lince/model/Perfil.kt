package com.equipo10.lince.model

/**
 * Resumen mensual (pantalla "Resumen").
 * Regla del equipo (evidencia Clase 2): los servicios CANCELADOS igual se pagan al recurso.
 */
data class ResumenMensual(
    val mes: String,                    // "Octubre 2026"
    val esMesActual: Boolean,
    val completados: Int,
    val cancelados: Int,
    val montoTotal: Int,                // completados + cancelados (pagados al 100 %)
    val puntualidad: Int?,              // % de check-in sin atraso; null si no hubo check-in
    val pagoEstimado: String,           // "05 de noviembre"
    val detalle: List<Servicio>         // servicios cerrados del mes, del más reciente al más antiguo
)

/** Datos que se muestran en la pantalla de Perfil. */
data class Perfil(
    val usuario: Usuario,
    val telefono: String?,                 // puede ser null -> se muestra "No registrado"
    val empresa: String = "Southbound",    // parámetro con valor por defecto
    val serviciosDelMes: Int
)
