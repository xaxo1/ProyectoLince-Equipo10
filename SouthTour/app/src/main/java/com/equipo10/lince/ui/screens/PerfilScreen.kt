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
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.BuildConfig
import com.equipo10.lince.R
import com.equipo10.lince.model.Perfil
import com.equipo10.lince.ui.components.Avatar
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.Etiqueta
import com.equipo10.lince.ui.components.TarjetaSouthTour
import com.equipo10.lince.viewmodel.PerfilViewModel

@Composable
fun PerfilScreen(
    idiomaActual: String,
    onCambiarIdioma: (String) -> Unit,
    onVerSouthbound: () -> Unit,
    onSesionCerrada: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.cargar() }

    val error = estado.error
    val perfil = estado.perfil
    when {
        estado.cargando -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = viewModel::cargar)
        perfil != null -> ContenidoPerfil(
            perfil = perfil,
            idiomaActual = idiomaActual,
            onCambiarIdioma = onCambiarIdioma,
            onVerSouthbound = onVerSouthbound,
            onCerrarSesion = { viewModel.cerrarSesion(onSesionCerrada) }
        )
    }
}

@Composable
private fun ContenidoPerfil(
    perfil: Perfil,
    idiomaActual: String,
    onCambiarIdioma: (String) -> Unit,
    onVerSouthbound: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario = perfil.usuario
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
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
                Text(stringResource(R.string.datos_recurso), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FilaDato("RUT", usuario.rut)
                FilaDato(stringResource(R.string.correo), usuario.correo)
                FilaDato(stringResource(R.string.empresa), perfil.empresa)
                usuario.recursoId?.let { FilaDato(stringResource(R.string.id_lince), "$it") }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                FilaDato(stringResource(R.string.servicios_mes), "${perfil.serviciosDelMes}")
                FilaDato(stringResource(R.string.hitos_registrados), "${perfil.hitosRegistrados}")
                FilaDato(stringResource(R.string.pendientes_sync), "${perfil.pendientesSincronizar}")
                FilaDato(stringResource(R.string.version), BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")")
            }
        }

        // Selector de idioma PERSISTENTE (DataStore): la app se recarga en el idioma elegido
        TarjetaSouthTour {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.idioma), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = idiomaActual == "es", onClick = { onCambiarIdioma("es") }, label = { Text("Español") })
                    FilterChip(selected = idiomaActual == "en", onClick = { onCambiarIdioma("en") }, label = { Text("English") })
                }
            }
        }

        OutlinedButton(onClick = onVerSouthbound, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(Icons.Filled.Public, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.catalogo_southbound), fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick = onCerrarSesion,
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.cerrar_sesion), fontWeight = FontWeight.SemiBold)
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
