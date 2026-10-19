package com.equipo10.lince.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/** Nombre visible de la app: si cambia, se modifica SOLO aquí. */
const val NOMBRE_APP = "SouthTour"

/** SEALED CLASS con todas las rutas: evita errores de tipeo en los strings de navegación. */
sealed class Ruta(val ruta: String, val titulo: String) {
    data object Login : Ruta("login", "Iniciar sesión")
    data object Inicio : Ruta("inicio", "Servicios del día")
    data object Servicios : Ruta("servicios", "Calendario de servicios")
    data object Perfil : Ruta("perfil", "Mi perfil")
    data object Disponibilidad : Ruta("disponibilidad", "Disponibilidad")

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
}

/** Todas las rutas que muestran la barra superior (todas menos Login). */
val rutasConBarraSuperior: List<Ruta> = listOf(
    Ruta.Inicio, Ruta.Servicios, Ruta.Perfil, Ruta.Disponibilidad, Ruta.Detalle, Ruta.Reserva
)

data class ItemMenu(val ruta: Ruta, val etiqueta: String, val icono: ImageVector)

/** Opciones de la NavigationBar inferior (flujo pedido: Inicio -> Servicios -> Perfil). */
val itemsMenu: List<ItemMenu> = listOf(
    ItemMenu(Ruta.Inicio, "Inicio", Icons.Filled.Home),
    ItemMenu(Ruta.Servicios, "Servicios", Icons.Filled.DateRange),
    ItemMenu(Ruta.Perfil, "Perfil", Icons.Filled.Person)
)
