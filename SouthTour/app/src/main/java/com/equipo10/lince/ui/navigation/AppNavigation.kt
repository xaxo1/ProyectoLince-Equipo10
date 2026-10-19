package com.equipo10.lince.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo10.lince.ui.screens.DetalleServicioScreen
import com.equipo10.lince.ui.screens.DisponibilidadScreen
import com.equipo10.lince.ui.screens.InicioScreen
import com.equipo10.lince.ui.screens.LoginScreen
import com.equipo10.lince.ui.screens.PerfilScreen
import com.equipo10.lince.ui.screens.ReservaScreen
import com.equipo10.lince.ui.screens.ServiciosScreen

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

    Scaffold(
        // En Login el fondo es café (pantalla de marca); en el resto, el fondo claro
        containerColor = if (enLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
        topBar = {
            if (rutaConBarra != null) {
                CenterAlignedTopAppBar(
                    title = { Text(rutaConBarra.titulo) },
                    navigationIcon = {
                        // Flecha "volver" en todas las pantallas menos Inicio
                        if (rutaActual != Ruta.Inicio.ruta) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        }
                    },
                    // Paleta SouthTour: TopAppBar en color principal (café)
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (esSeccionDelMenu) {
                // Paleta SouthTour: NavigationBar café, ítem seleccionado en turquesa
                NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                    itemsMenu.forEach { item ->
                        NavigationBarItem(
                            selected = rutaActual == item.ruta.ruta,
                            onClick = { navController.navegarSeccion(item.ruta) },
                            icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                            label = { Text(item.etiqueta) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.secondary,
                                unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
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
                            // Saca el Login de la pila: "atrás" desde Inicio no vuelve al Login
                            popUpTo(Ruta.Login.ruta) { inclusive = true }
                        }
                    }
                )
            }
            composable(Ruta.Inicio.ruta) {
                InicioScreen(
                    onVerDetalle = { id -> navController.navigate(Ruta.Detalle.crear(id)) },
                    onVerCalendario = { navController.navegarSeccion(Ruta.Servicios) },
                    onVerDisponibilidad = { navController.navigate(Ruta.Disponibilidad.ruta) { launchSingleTop = true } }
                )
            }
            composable(Ruta.Servicios.ruta) {
                ServiciosScreen(
                    onVerDetalle = { id -> navController.navigate(Ruta.Detalle.crear(id)) }
                )
            }
            composable(
                route = Ruta.Detalle.ruta,
                arguments = listOf(navArgument(Ruta.Detalle.ARG_ID) { type = NavType.IntType })
            ) { entrada ->
                val servicioId = entrada.arguments?.getInt(Ruta.Detalle.ARG_ID) ?: -1
                DetalleServicioScreen(
                    servicioId = servicioId,
                    onVerReserva = { numero -> navController.navigate(Ruta.Reserva.crear(numero)) }
                )
            }
            composable(
                route = Ruta.Reserva.ruta,
                arguments = listOf(navArgument(Ruta.Reserva.ARG_NUMERO) { type = NavType.StringType })
            ) { entrada ->
                val numero = entrada.arguments?.getString(Ruta.Reserva.ARG_NUMERO).orEmpty()
                ReservaScreen(numeroReserva = numero)
            }
            composable(Ruta.Disponibilidad.ruta) {
                DisponibilidadScreen()
            }
            composable(Ruta.Perfil.ruta) {
                PerfilScreen(
                    onVerDisponibilidad = { navController.navigate(Ruta.Disponibilidad.ruta) { launchSingleTop = true } },
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
