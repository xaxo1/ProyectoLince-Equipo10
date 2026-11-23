package com.equipo10.lince.model

/** Tipos de servicio de la Lince API. TRANSFER y EXCURSION llevan transportista; GUIDED_SERVICE, guía. */
enum class TipoServicio(val etiqueta: String) {
    TRANSFER("Transfer"),
    GUIDED_SERVICE("Servicio guiado"),
    EXCURSION("Excursión");

    companion object {
        fun desde(codigo: String?): TipoServicio = entries.find { it.name == codigo } ?: TRANSFER
    }
}

/**
 * Servicio asignado, armado con DOS fuentes:
 * - Lince API del cliente (solo lectura): datos del servicio, reserva, estados CF/RQ/WL/XX y StatusPL.
 * - Backend propio: check-in / check-out ya registrados y la confirmación del recurso.
 */
data class Servicio(
    val id: Int,
    val codigo: String,
    val tipo: TipoServicio,
    val fecha: String,                          // "yyyy-MM-dd"
    val horaPickup: String,                     // "HH:mm" (hora de Chile)
    val excursion: String,                      // descripción del servicio
    val agencia: String,                        // cliente
    val numeroReserva: String,
    val titularReserva: String,
    val puntoPickup: String,
    val dropOff: String,
    val pasajeros: Int,
    val paxTexto: String,
    val estadoLince: String,                    // CF, RQ, WL, XX
    val statusPL: String?,                      // CF, RQ, DL, CX o null (sin asignar)
    val oficina: String,
    val idiomaRequerido: String? = null,
    val notas: String? = null,
    val guia: String? = null,
    val transportista: String? = null,
    val vehiculo: String? = null,
    // ----- Calculado con los hitos del backend -----
    val estado: EstadoServicio = EstadoServicio.CONFIRMADO,
    val horaCheckIn: String? = null,
    val horaCheckOut: String? = null,
    val resultadoCheckIn: String? = null,       // VERIFICADA, FUERA_DE_ZONA, ATRASADA, GPS_IMPRECISO
    val conAtraso: Boolean = false,
    val confirmacion: String? = null,           // CONFIRMADA, RECHAZADA o null
    val sincronizacionPendiente: Boolean = false
) {
    /** Búsqueda del Calendario: excursión, agencia, N° de reserva o punto de pickup (sin importar mayúsculas). */
    fun coincideConBusqueda(texto: String): Boolean =
        listOf(excursion, agencia, numeroReserva, puntoPickup, codigo).any { it.contains(texto, ignoreCase = true) }
}

/** Descripción de los códigos de estado de la Lince API (catálogo estadosServicio / statusPL). */
object EstadosLince {
    fun servicio(codigo: String?): String = when (codigo) {
        "CF" -> "Confirmado"
        "RQ" -> "En solicitud"
        "WL" -> "Lista de espera"
        "XX" -> "Cancelado"
        else -> "Sin estado"
    }

    fun asignacion(codigo: String?): String = when (codigo) {
        "CF" -> "Confirmado por el staff"
        "RQ" -> "En solicitud"
        "DL" -> "Eliminado"
        "CX" -> "Cancelado"
        else -> "Sin asignar"
    }
}

/** FUNCIÓN DE EXTENSIÓN + groupBy: agrupa los servicios por fecha (para el Calendario). */
fun List<Servicio>.agruparPorFecha(): Map<String, List<Servicio>> = groupBy { it.fecha }
