package com.equipo10.lince.model

/** Tipos de hito que registra el teléfono en el backend. */
enum class TipoHito { CHECK_IN, CHECK_OUT, POSICION }

/**
 * Resultado de registrar un check-in / check-out: lo que respondió el servidor
 * (o, si no había internet, lo que quedó guardado en Room esperando sincronizar).
 */
data class RegistroHito(
    val tipo: TipoHito,
    val resultado: String,           // VERIFICADA, FUERA_DE_ZONA, ATRASADA, GPS_IMPRECISO, REGISTRADA, PENDIENTE
    val mensaje: String,
    val distanciaMetros: Double?,
    val radioMetros: Int?,
    val precisionMetros: Double,
    val hora: String,                // "HH:mm" (del servidor; del teléfono si quedó pendiente)
    val pendiente: Boolean = false
) {
    val verificado: Boolean get() = resultado == "VERIFICADA" || resultado == "REGISTRADA"
}

/** Hito visto por coordinación (panel operativo). */
data class HitoPanel(
    val servicioId: Int,
    val usuario: String,
    val tipo: String,
    val resultado: String,
    val mensaje: String,
    val lugar: String?,
    val distanciaMetros: Double?,
    val latitud: Double,
    val longitud: Double,
    val hora: String
)

/** Geocerca de un hotel/punto de pickup (catálogo del backend). */
data class GeocercaInfo(
    val id: Long,
    val nombre: String,
    val oficina: String,
    val latitud: Double,
    val longitud: Double,
    val radioMetros: Int
)

/** Reglas vigentes del backend (para mostrarlas en pantalla). */
data class Reglas(
    val radioDefecto: Int = 150,
    val precisionMaxima: Double = 50.0,
    val minutosAntes: Int = 15,
    val diasAnticipacion: Int = 7
)
