package com.equipo10.lince.model

/** Tipos de incidencia que se pueden reportar en terreno. */
enum class TipoIncidencia(val etiqueta: String) {
    ATRASO_PASAJEROS("Atraso de pasajeros"),
    PASAJERO_AUSENTE("Pasajero ausente"),
    PROBLEMA_VEHICULO("Problema con el vehículo"),
    CAMBIO_ITINERARIO("Cambio de itinerario"),
    OTRO("Otro")
}

/** Incidencia reportada por el guía o transportista durante un servicio (se guarda con Room). */
data class Incidencia(
    val id: Long,
    val servicioId: Int,
    val tipo: TipoIncidencia,
    val descripcion: String,
    val pasajerosPresentes: Int,
    val rutaFoto: String?,          // null si no se tomó foto
    val registradaEn: String        // "yyyy-MM-dd HH:mm"
)
