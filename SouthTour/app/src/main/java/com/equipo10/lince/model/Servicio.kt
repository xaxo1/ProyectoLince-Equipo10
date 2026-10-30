package com.equipo10.lince.model

/** Tipo de servicio: define el ícono y el texto de la tarjeta. */
enum class TipoServicio(val etiqueta: String) {
    TRASLADO("Traslado"),
    TOUR("Tour")
}

/**
 * DATA CLASS: Kotlin genera toString(), equals(), hashCode() y copy().
 * Contiene lo que muestra el "Detalle del servicio" y el registro de check-in / check-out.
 */
data class Servicio(
    val id: Int,
    val excursion: String,
    val agencia: String,
    val numeroReserva: String,
    val fecha: String,                          // formato "yyyy-MM-dd"
    val horaPickup: String,                     // "08:00"
    val puntoPickup: String,
    val dropOff: String,
    val pasajeros: Int,
    val contraparte: String? = null,            // NULL SAFETY: puede no estar asignada aún
    val requisitos: List<String> = emptyList(),
    val monto: Int,                             // pago del servicio en CLP
    val estado: EstadoServicio = EstadoServicio.CONFIRMADO,
    val horaCheckIn: String? = null,            // se completa al iniciar el servicio
    val horaCheckOut: String? = null,           // se completa al finalizar el servicio
    val conAtraso: Boolean = false,             // true si el check-in no fue 15 min antes del pickup
    val tipo: TipoServicio = TipoServicio.TOUR,
    val vehiculo: String? = null,               // vehículo asignado al servicio
    val motivoCancelacion: String? = null,      // solo si está CANCELADO
    val ubicacionCheckIn: String? = null,       // coordenadas GPS del check-in
    val ubicacionCheckOut: String? = null       // coordenadas GPS del check-out
) : Informable {

    override fun informar(): String = "$excursion · $horaPickup · $pasajeros pasajeros"

    /** Búsqueda del Calendario: excursión, agencia, N° de reserva o punto de pickup (sin importar mayúsculas). */
    fun coincideConBusqueda(texto: String): Boolean =
        listOf(excursion, agencia, numeroReserva, puntoPickup).any { it.contains(texto, ignoreCase = true) }
}

/** FUNCIÓN DE EXTENSIÓN + groupBy: agrupa los servicios por fecha (para el Calendario). */
fun List<Servicio>.agruparPorFecha(): Map<String, List<Servicio>> =
    groupBy { it.fecha }

/** FUNCIÓN DE EXTENSIÓN + fold (reducción): suma los montos de la lista. */
fun List<Servicio>.montoTotal(): Int =
    fold(0) { acumulado, servicio -> acumulado + servicio.monto }
