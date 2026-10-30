package com.equipo10.lince.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.model.Guia
import com.equipo10.lince.model.Perfil
import com.equipo10.lince.model.Transportista
import com.equipo10.lince.ui.components.Avatar
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.Etiqueta
import com.equipo10.lince.ui.components.TarjetaSouthTour
import com.equipo10.lince.viewmodel.PerfilViewModel

/** Pantalla "Mi perfil" (se abre desde el avatar de la barra superior). */
@Composable
fun PerfilScreen(
    onSesionCerrada: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.cargar() }

    // Variables locales para que Kotlin haga "smart cast" (de String? a String)
    val error = estado.error
    val perfil = estado.perfil

    when {
        estado.cargando -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = viewModel::cargar)
        perfil != null -> ContenidoPerfil(
            perfil = perfil,
            onCerrarSesion = {
                viewModel.cerrarSesion()
                onSesionCerrada()
            }
        )
    }
}

@Composable
private fun ContenidoPerfil(perfil: Perfil, onCerrarSesion: () -> Unit) {
    val usuario = perfil.usuario

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        Avatar(usuario, tamano = 96.dp)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(usuario.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Etiqueta(usuario.rol.etiqueta, MaterialTheme.colorScheme.secondary)
        }

        TarjetaSouthTour {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Datos del recurso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FilaDato("RUT", usuario.rut)
                FilaDato("Correo", usuario.email)
                FilaDato("Empresa", perfil.empresa)
                FilaDato("Teléfono", perfil.telefono ?: "No registrado")   // operador Elvis
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                // POLIMORFISMO + smart cast con "is": cada tipo de usuario muestra datos distintos
                when (usuario) {
                    is Guia -> FilaDato("Idiomas", usuario.idiomas.joinToString(", "))
                    is Transportista -> {
                        FilaDato("Licencia", usuario.licencia)
                        FilaDato("Vehículo", usuario.vehiculo ?: "Sin vehículo asignado")
                    }
                }
                FilaDato("Servicios este mes", "${perfil.serviciosDelMes}")
            }
        }

        TarjetaSouthTour {
            Column(Modifier.padding(16.dp)) {
                Text("Resumen del recurso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                // informar() se comporta distinto si es Guia o Transportista (polimorfismo)
                Text(usuario.informar(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        OutlinedButton(
            onClick = onCerrarSesion,
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(valor, fontWeight = FontWeight.Medium)
    }
}
