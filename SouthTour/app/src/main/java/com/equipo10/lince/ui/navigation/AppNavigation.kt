package com.equipo10.lince.ui.navigation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.ui.components.Avatar
import com.equipo10.lince.ui.screens.DetalleServicioScreen
import com.equipo10.lince.ui.screens.DisponibilidadScreen
import com.equipo10.lince.ui.screens.IncidenciaScreen
import com.equipo10.lince.ui.screens.InicioScreen
import com.equipo10.lince.ui.screens.LoginScreen
import com.equipo10.lince.ui.screens.PerfilScreen
import com.equipo10.lince.ui.screens.ReservaScreen
import com.equipo10.lince.ui.screens.ResumenScreen
import com.equipo10.lince.ui.screens.ServiciosScreen
import com.equipo10.lince.ui.theme.ArenaSouthTour

/** Raíz de la app: Scaffold (TopAppBar + NavigationBar) + NavHost con todas las rutas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinceApp() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val rutaConBarra = rutasConBarraSuperior.find { it.ruta == rutaActual }
    val esSeccionDelMenu = itemsMenu.any { it.ruta.ruta == rutaActual }
    val enLogin = rutaConBarra == null
    val usuario = RepositorioLince.usuarioActual

    // Íconos de la barra de estado: oscuros en el Login (fondo claro), blancos sobre la barra café
    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = (vista.context as Activity).window
            WindowCompat.getInsetsController(ventana, vista).isAppearanceLightStatusBars = enLogin
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (rutaConBarra != null) {
                TopAppBar(
                    title = {
                        Column {
                            Text(rutaConBarra.titulo, fontWeight = FontWeight.Bold)
                            // En las secciones del menú se muestra quién está conectado
                            if (esSeccionDelMenu && usuario != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(7.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "${usuario.nombre} · ${usuario.rol.etiqueta}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ArenaSouthTour
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        if (esSeccionDelMenu) {
                            // Ícono de marca en las secciones principales
                            Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.padding(start = 16.dp, end = 4.dp))
                        } else {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        }
                    },
                    actions = {
                        // Avatar: abre "Mi perfil" (decisión de navegación de la Clase 2)
                        if (rutaActual != Ruta.Perfil.ruta) {
                            IconButton(onClick = {
                                navController.navigate(Ruta.Perfil.ruta) { launchSingleTop = true }
                            }) {
                                Avatar(usuario, tamano = 34.dp)
                            }
                        }
                    },
                    // Paleta SouthTour: TopAppBar en color principal (café)
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (esSeccionDelMenu) {
                // NavigationBar café; ítem activo con píldora turquesa (sistema "Atacama Fieldwork")
                NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                    itemsMenu.forEach { item ->
                        NavigationBarItem(
                            selected = rutaActual == item.ruta.ruta,
                            onClick = { navController.navegarSeccion(item.ruta) },
                            icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                            label = { Text(item.etiqueta, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.secondary,
                                unselectedIconColor = ArenaSouthTour,
                                unselectedTextColor = ArenaSouthTour
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Ruta.Login.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Ruta.Login.ruta) {
                LoginScreen(
                    onLoginExitoso = {
                        navController.navigate(Ruta.Inicio.ruta) {
                            // Saca el Login de la pila: "atrás" desde Hoy no vuelve al Login
                            popUpTo(Ruta.Login.ruta) { inclusive = true }
                        }
                    }
                )
            }
            composable(Ruta.Inicio.ruta) {
                InicioScreen(onVerDetalle = { id -> navController.navigate(Ruta.Detalle.crear(id)) })
            }
            composable(Ruta.Servicios.ruta) {
                ServiciosScreen(onVerDetalle = { id -> navController.navigate(Ruta.Detalle.crear(id)) })
            }
            composable(Ruta.Disponibilidad.ruta) {
                DisponibilidadScreen()
            }
            composable(Ruta.Resumen.ruta) {
                ResumenScreen()
            }
            composable(
                route = Ruta.Detalle.ruta,
                arguments = listOf(navArgument(Ruta.Detalle.ARG_ID) { type = NavType.IntType })
            ) { entrada ->
                val servicioId = entrada.arguments?.getInt(Ruta.Detalle.ARG_ID) ?: -1
                DetalleServicioScreen(
                    servicioId = servicioId,
                    onVerReserva = { numero -> navController.navigate(Ruta.Reserva.crear(numero)) },
                    onReportarIncidencia = { id -> navController.navigate(Ruta.Incidencia.crear(id)) }
                )
            }
            composable(
                route = Ruta.Incidencia.ruta,
                arguments = listOf(navArgument(Ruta.Incidencia.ARG_ID) { type = NavType.IntType })
            ) { entrada ->
                val servicioId = entrada.arguments?.getInt(Ruta.Incidencia.ARG_ID) ?: -1
                IncidenciaScreen(servicioId = servicioId, onListo = { navController.popBackStack() })
            }
            composable(
                route = Ruta.Reserva.ruta,
                arguments = listOf(navArgument(Ruta.Reserva.ARG_NUMERO) { type = NavType.StringType })
            ) { entrada ->
                val numero = entrada.arguments?.getString(Ruta.Reserva.ARG_NUMERO).orEmpty()
                ReservaScreen(numeroReserva = numero)
            }
            composable(Ruta.Perfil.ruta) {
                PerfilScreen(
                    onSesionCerrada = {
                        navController.navigate(Ruta.Login.ruta) {
                            // Limpia toda la pila al cerrar sesión
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

/**
 * FUNCIÓN DE EXTENSIÓN sobre NavHostController para moverse entre las secciones del menú
 * sin apilar pantallas repetidas.
 */
fun NavHostController.navegarSeccion(ruta: Ruta) {
    navigate(ruta.ruta) {
        popUpTo(Ruta.Inicio.ruta) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
