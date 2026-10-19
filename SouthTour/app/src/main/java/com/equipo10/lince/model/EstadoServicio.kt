package com.equipo10.lince.model

/**
 * Estados de un servicio ya asignado por operaciones.
 * (La app NO asigna servicios: solo los muestra.)
 */
enum class EstadoServicio(val etiqueta: String) {
    CONFIRMADO("Confirmado"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado")
}
