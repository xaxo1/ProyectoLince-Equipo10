package com.equipo10.lince.util

import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Utilidades de fechas, horas y montos.
 * Se usa java.util (Calendar / SimpleDateFormat) porque funciona desde API 24,
 * que es el Minimum SDK del proyecto (java.time requiere API 26).
 */
object Formatos {

    private val chile: Locale = Locale.forLanguageTag("es-CL")
    private val formatoIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val formatoHora = SimpleDateFormat("HH:mm", Locale.US)
    private const val MILIS_POR_HORA = 3_600_000.0

    /** Fecha de hoy en formato "yyyy-MM-dd". */
    fun hoy(): String = formatoIso.format(Date())

    /** Hora actual en formato "HH:mm" (se usa en el check-in / check-out). */
    fun horaActual(): String = formatoHora.format(Date())

    /** Fecha y hora actual "yyyy-MM-dd HH:mm" (registro de incidencias). */
    fun fechaHoraActual(): String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    /** Fecha a "dias" de hoy (positivo = futuro, negativo = pasado). */
    fun fechaEnDias(dias: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, dias)
        return formatoIso.format(calendario.time)
    }

    /**
     * Fecha pasada que siempre cae dentro del mes actual
     * (así el Resumen mensual siempre tiene datos ficticios que mostrar).
     */
    fun fechaPasadaDelMes(diasAtras: Int): String {
        val calendario = Calendar.getInstance()
        val mesActual = calendario.get(Calendar.MONTH)
        calendario.add(Calendar.DAY_OF_YEAR, -diasAtras)
        if (calendario.get(Calendar.MONTH) != mesActual) {
            calendario.time = Date()
            calendario.set(Calendar.DAY_OF_MONTH, 1)
        }
        return formatoIso.format(calendario.time)
    }

    /** Días que faltan desde hoy hasta la fecha (0 = hoy, negativo = pasado). */
    fun diasDesdeHoy(fechaIso: String): Int {
        val destino = try { formatoIso.parse(fechaIso) } catch (e: ParseException) { null } ?: return 0
        val hoy = formatoIso.parse(hoy()) ?: return 0
        return ((destino.time - hoy.time) / MILIS_POR_HORA / 24).roundToInt()
    }

    /** "08:30" -> 510 (minutos desde la medianoche). Si el texto no es válido devuelve null. */
    fun minutosDelDia(hora: String): Int? {
        val partes = hora.split(":")
        if (partes.size != 2) return null
        val horas = partes[0].toIntOrNull() ?: return null
        val minutos = partes[1].toIntOrNull() ?: return null
        return horas * 60 + minutos
    }

    /**
     * Regla del flujo UML: el check-in debe hacerse al menos 15 minutos ANTES del pickup.
     * Devuelve true si se registró con atraso.
     */
    fun llegaConAtraso(horaCheckIn: String, horaPickup: String): Boolean {
        val llegada = minutosDelDia(horaCheckIn) ?: return false
        val pickup = minutosDelDia(horaPickup) ?: return false
        return llegada > pickup - 15
    }

    /** "2026-09" -> sirve para filtrar los servicios del mes actual. */
    fun mesActualIso(): String = hoy().substring(0, 7)

    /** "Septiembre 2026" */
    fun nombreMesActual(): String = nombreMes(0)

    /** Calendario posicionado en el día 1 del mes actual + desplazamiento (-1 = mes anterior). */
    private fun inicioDeMes(desplazamiento: Int): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, desplazamiento)
        }

    /** desplazamiento 0 -> "2026-10", -1 -> "2026-09" */
    fun mesIso(desplazamiento: Int): String =
        formatoIso.format(inicioDeMes(desplazamiento).time).substring(0, 7)

    /** desplazamiento 0 -> "Octubre 2026" */
    fun nombreMes(desplazamiento: Int): String =
        SimpleDateFormat("MMMM yyyy", chile).format(inicioDeMes(desplazamiento).time)
            .replaceFirstChar { it.titlecase(chile) }

    /** Fecha estimada de pago: el día 5 del mes siguiente -> "05 de noviembre" */
    fun fechaPagoEstimado(desplazamiento: Int): String {
        val pago = inicioDeMes(desplazamiento + 1).apply { set(Calendar.DAY_OF_MONTH, 5) }
        return SimpleDateFormat("dd 'de' MMMM", chile).format(pago.time)
    }

    /** "2026-10-24" -> "24 oct" */
    fun diaMes(fechaIso: String): String =
        try {
            val fecha = formatoIso.parse(fechaIso)
            if (fecha == null) fechaIso else SimpleDateFormat("d MMM", chile).format(fecha).trimEnd('.')
        } catch (e: ParseException) {
            fechaIso
        }

    /** "2026-09-28" -> "Lunes 28 de septiembre". Si el texto no es válido, lo devuelve tal cual. */
    fun fechaLegible(fechaIso: String): String =
        try {
            val fecha = formatoIso.parse(fechaIso)
            if (fecha == null) fechaIso
            else SimpleDateFormat("EEEE d 'de' MMMM", chile).format(fecha).replaceFirstChar { it.titlecase(chile) }
        } catch (e: ParseException) {
            fechaIso
        }

    /** "2026-09-28" -> "Lun 28 sep" (versión corta para listas). */
    fun fechaCorta(fechaIso: String): String =
        try {
            val fecha = formatoIso.parse(fechaIso)
            if (fecha == null) fechaIso
            else SimpleDateFormat("EEE d MMM", chile).format(fecha).replaceFirstChar { it.titlecase(chile) }
        } catch (e: ParseException) {
            fechaIso
        }

    /** 55000 -> "$55.000" */
    fun pesos(monto: Int): String = "$" + NumberFormat.getIntegerInstance(chile).format(monto)
}
