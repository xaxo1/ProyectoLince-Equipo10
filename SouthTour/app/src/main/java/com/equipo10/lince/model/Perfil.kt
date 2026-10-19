package com.equipo10.lince.model

/** Resumen mensual: servicios realizados, cancelados y monto acumulado. */
data class ResumenMensual(
    val mes: String,
    val realizados: Int,
    val cancelados: Int,
    val montoAcumulado: Int
)

/** Datos que se muestran en la pantalla de Perfil. */
data class Perfil(
    val usuario: Usuario,
    val telefono: String?,                 // puede ser null -> se muestra "No registrado"
    val empresa: String = "Southbound",    // parámetro con valor por defecto
    val resumen: ResumenMensual
)
