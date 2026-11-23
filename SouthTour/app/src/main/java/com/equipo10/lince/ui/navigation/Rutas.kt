package com.equipo10.lince.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import com.equipo10.lince.R

/** Nombre visible de la app: si cambia, se modifica SOLO aquí. */
const val NOMBRE_APP = "SouthTour"

/** SEALED CLASS con todas las rutas. El título es un recurso de texto (español / inglés). */
sealed class Ruta(val ruta: String, @StringRes val titulo: Int) {
    data object Login : Ruta("login", R.string.titulo_login)
    data object Inicio : Ruta("hoy", R.string.nav_hoy)
    data object Servicios : Ruta("calendario", R.string.nav_calendario)
    data object Disponibilidad : Ruta("disponibilidad", R.string.nav_disponibilidad)
    data object Resumen : Ruta("resumen", R.string.nav_resumen)
    data object Perfil : Ruta("perfil", R.string.titulo_perfil)
    data object Coordinacion : Ruta("coordinacion", R.string.nav_panel)
    data object Southbound : Ruta("southbound", R.string.catalogo_southbound)

    /** Ruta con ARGUMENTO Int: el id del servicio de la Lince API ("detalle/3129"). */
    data object Detalle : Ruta("detalle/{servicioId}", R.string.titulo_detalle) {
        const val ARG_ID = "servicioId"
        fun crear(servicioId: Int): String = "detalle/$servicioId"
    }

    data object Reserva : Ruta("reserva/{servicioId}", R.string.titulo_reserva) {
        const val ARG_ID = "servicioId"
        fun crear(servicioId: Int): String = "reserva/$servicioId"
    }

    data object Incidencia : Ruta("incidencia/{servicioId}", R.string.reportar_incidencia) {
        const val ARG_ID = "servicioId"
        fun crear(servicioId: Int): String = "incidencia/$servicioId"
    }
}

/** Todas las rutas que muestran la barra superior (todas menos Login). */
val rutasConBarraSuperior: List<Ruta> = listOf(
    Ruta.Inicio, Ruta.Servicios, Ruta.Disponibilidad, Ruta.Resumen, Ruta.Perfil,
    Ruta.Detalle, Ruta.Reserva, Ruta.Incidencia, Ruta.Coordinacion, Ruta.Southbound
)

data class ItemMenu(val ruta: Ruta, @StringRes val etiqueta: Int, val icono: ImageVector)

/** Menú de guías y transportistas. */
val menuRecurso: List<ItemMenu> = listOf(
    ItemMenu(Ruta.Inicio, R.string.nav_hoy, Icons.Filled.Today),
    ItemMenu(Ruta.Servicios, R.string.nav_calendario, Icons.Filled.DateRange),
    ItemMenu(Ruta.Disponibilidad, R.string.nav_disponibilidad, Icons.Filled.EventAvailable),
    ItemMenu(Ruta.Resumen, R.string.nav_resumen, Icons.Filled.Assessment)
)

/** Menú de coordinación. */
val menuCoordinacion: List<ItemMenu> = listOf(
    ItemMenu(Ruta.Coordinacion, R.string.nav_panel, Icons.Filled.Dashboard),
    ItemMenu(Ruta.Southbound, R.string.nav_southbound, Icons.Filled.Public)
)
