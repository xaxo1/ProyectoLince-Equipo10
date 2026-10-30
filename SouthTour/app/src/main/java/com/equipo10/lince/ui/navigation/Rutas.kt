package com.equipo10.lince.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

/** Nombre visible de la app: si cambia, se modifica SOLO aquí. */
const val NOMBRE_APP = "SouthTour"

/** SEALED CLASS con todas las rutas: evita errores de tipeo en los strings de navegación. */
sealed class Ruta(val ruta: String, val titulo: String) {
    data object Login : Ruta("login", "Iniciar sesión")
    data object Inicio : Ruta("hoy", "Hoy")
    data object Servicios : Ruta("calendario", "Calendario")
    data object Disponibilidad : Ruta("disponibilidad", "Disponibilidad")
    data object Resumen : Ruta("resumen", "Resumen")
    data object Perfil : Ruta("perfil", "Mi perfil")

    /** Ruta con ARGUMENTO Int: el id del servicio viaja en la ruta ("detalle/101"). */
    data object Detalle : Ruta("detalle/{servicioId}", "Detalle del servicio") {
        const val ARG_ID = "servicioId"
        fun crear(servicioId: Int): String = "detalle/$servicioId"
    }

    /** Ruta con ARGUMENTO String: el número de reserva ("reserva/SB-24031"). */
    data object Reserva : Ruta("reserva/{numeroReserva}", "Reserva completa") {
        const val ARG_NUMERO = "numeroReserva"
        fun crear(numero: String): String = "reserva/$numero"
    }

    /** Formulario de incidencia de un servicio ("incidencia/101"). */
    data object Incidencia : Ruta("incidencia/{servicioId}", "Reportar incidencia") {
        const val ARG_ID = "servicioId"
        fun crear(servicioId: Int): String = "incidencia/$servicioId"
    }
}

/** Todas las rutas que muestran la barra superior (todas menos Login). */
val rutasConBarraSuperior: List<Ruta> = listOf(
    Ruta.Inicio, Ruta.Servicios, Ruta.Disponibilidad, Ruta.Resumen,
    Ruta.Perfil, Ruta.Detalle, Ruta.Reserva, Ruta.Incidencia
)

data class ItemMenu(val ruta: Ruta, val etiqueta: String, val icono: ImageVector)

/**
 * NavigationBar con los 4 destinos definidos en la evidencia de la Clase 2:
 * Hoy, Calendario, Disponibilidad y Resumen. El Perfil se abre desde el avatar.
 */
val itemsMenu: List<ItemMenu> = listOf(
    ItemMenu(Ruta.Inicio, "Hoy", Icons.Filled.Today),
    ItemMenu(Ruta.Servicios, "Calendario", Icons.Filled.DateRange),
    ItemMenu(Ruta.Disponibilidad, "Disponibilidad", Icons.Filled.EventAvailable),
    ItemMenu(Ruta.Resumen, "Resumen", Icons.Filled.Assessment)
)
