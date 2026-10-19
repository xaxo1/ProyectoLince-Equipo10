package com.equipo10.lince.model

/** Estado de un día en la pantalla de Disponibilidad. */
enum class EstadoDia(val etiqueta: String) {
    LIBRE("Libre"),
    CON_SERVICIO("Con servicio"),
    BLOQUEADO("Bloqueado")
}

data class DiaDisponibilidad(
    val fecha: String,                  // "yyyy-MM-dd"
    val diasDeAnticipacion: Int,        // 0 = hoy, 1 = mañana...
    val estado: EstadoDia,
    val servicio: String? = null        // excursión del día, si tiene servicio
)
