package com.equipo10.lince.model

/**
 * Tipos de incidencia. Los [manual] = true se eligen en el formulario; los demás los crea
 * el backend automáticamente cuando rechaza un check-in.
 */
enum class TipoIncidencia(val etiqueta: String, val manual: Boolean = true) {
    ATRASO_PASAJEROS("Atraso de pasajeros"),
    PASAJERO_AUSENTE("Pasajero ausente"),
    PROBLEMA_VEHICULO("Problema con el vehículo"),
    CAMBIO_ITINERARIO("Cambio de itinerario"),
    OTRO("Otro"),
    ATRASO("Llegada tardía", manual = false),
    FUERA_DE_ZONA("Fuera de la geocerca", manual = false),
    GPS_IMPRECISO("GPS impreciso", manual = false);

    companion object {
        fun desde(codigo: String?): TipoIncidencia = entries.find { it.name == codigo } ?: OTRO
        val manuales: List<TipoIncidencia> get() = entries.filter { it.manual }
    }
}

/** Incidencia guardada en el backend (la foto, si existe, queda en el teléfono). */
data class Incidencia(
    val id: Long,
    val servicioId: Int,
    val tipo: TipoIncidencia,
    val descripcion: String,
    val pasajerosPresentes: Int?,
    val automatica: Boolean,
    val estado: String,             // ABIERTA o CERRADA
    val registradaEn: String,       // "yyyy-MM-dd HH:mm"
    val autor: String,
    val rutaFoto: String? = null
)
