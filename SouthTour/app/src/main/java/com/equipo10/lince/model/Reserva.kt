package com.equipo10.lince.model

/** Una actividad del itinerario completo de los pasajeros. */
data class ActividadItinerario(
    val fecha: String,          // "yyyy-MM-dd"
    val hora: String,           // "08:00" o "Libre"
    val descripcion: String
)

/** "Reserva completa": datos del grupo e itinerario, buscada por número de reserva. */
data class Reserva(
    val numero: String,
    val agencia: String,
    val titular: String,
    val nacionalidad: String,
    val totalPasajeros: Int,
    val contactoAgencia: String,
    val observaciones: List<String> = emptyList(),
    val itinerario: List<ActividadItinerario> = emptyList()
)
