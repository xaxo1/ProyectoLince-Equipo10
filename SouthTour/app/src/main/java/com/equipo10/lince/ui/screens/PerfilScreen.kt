package com.equipo10.lince.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.model.Guia
import com.equipo10.lince.model.Perfil
import com.equipo10.lince.model.Transportista
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.viewmodel.PerfilViewModel

@Composable
fun PerfilScreen(
    onVerDisponibilidad: () -> Unit,
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
            onVerDisponibilidad = onVerDisponibilidad,
            onCerrarSesion = {
                viewModel.cerrarSesion()
                onSesionCerrada()
            }
        )
    }
}

@Composable
private fun ContenidoPerfil(perfil: Perfil, onVerDisponibilidad: () -> Unit, onCerrarSesion: () -> Unit) {
    val usuario = perfil.usuario
    val resumen = perfil.resumen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(88.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    usuario.iniciales,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(usuario.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(usuario.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(usuario.rol.etiqueta, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FilaDato("Empresa", perfil.empresa)
                FilaDato("Teléfono", perfil.telefono ?: "No registrado")   // operador Elvis

                // POLIMORFISMO + smart cast con "is": cada tipo de usuario muestra datos distintos
                when (usuario) {
                    is Guia -> FilaDato("Idiomas", usuario.idiomas.joinToString(", "))
                    is Transportista -> {
                        FilaDato("Licencia", usuario.licencia)
                        FilaDato("Vehículo", usuario.vehiculo ?: "Sin vehículo asignado")
                    }
                }
            }
        }

        // Resumen mensual (pantalla 8 del diseño; se actualiza con cada check-out): realizados, cancelados y monto acumulado
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Resumen de ${resumen.mes}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Indicador("Realizados", "${resumen.realizados}", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                    Indicador("Cancelados", "${resumen.cancelados}", MaterialTheme.colorScheme.error, Modifier.weight(1f))
                }
                FilaDato("Monto acumulado", Formatos.pesos(resumen.montoAcumulado))
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Resumen del usuario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                // informar() se comporta distinto si es Guia o Transportista (polimorfismo)
                Text(usuario.informar(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        OutlinedButton(onClick = onVerDisponibilidad, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Gestionar disponibilidad")
        }

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
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

@Composable
private fun Indicador(titulo: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Surface(color = color.copy(alpha = 0.12f), shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
