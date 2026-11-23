package com.equipo10.lince.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.equipo10.lince.R
import com.equipo10.lince.model.HitoPanel
import com.equipo10.lince.ui.components.BannerMensaje
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.Etiqueta
import com.equipo10.lince.viewmodel.CoordinacionViewModel
import java.util.Locale
import kotlin.math.roundToInt

/** Panel de COORDINACIÓN: hitos del día, última posición de cada transportista, incidencias y radios. */
@Composable
fun CoordinacionScreen(viewModel: CoordinacionViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.cargar() }

    val error = estado.error
    when {
        estado.cargando -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = viewModel::cargar)
        else -> LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.hitos_hoy), style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Etiqueta("${estado.verificados} ✓", MaterialTheme.colorScheme.tertiary)
                    Etiqueta("${estado.rechazados} ✗", MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::cargar) { Text(stringResource(R.string.actualizar)) }
                }
                estado.mensaje?.let { BannerMensaje(texto = it, esAlerta = false) }
            }
            if (estado.hitos.isEmpty()) item { Text(stringResource(R.string.sin_hitos), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(estado.hitos) { FilaHito(it) }

            item { Titulo(stringResource(R.string.ultimas_posiciones)) }
            if (estado.posiciones.isEmpty()) item { Text("—") }
            items(estado.posiciones) { FilaHito(it, mostrarCoordenadas = true) }

            item { Titulo(stringResource(R.string.incidencias_titulo, estado.incidencias.size)) }
            items(estado.incidencias, key = { "inc-${it.id}" }) { inc ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${inc.tipo.etiqueta} · servicio ${inc.servicioId}", fontWeight = FontWeight.SemiBold,
                            color = if (inc.automatica) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                        Text(inc.descripcion, style = MaterialTheme.typography.bodyMedium)
                        Text("${inc.autor} · ${inc.registradaEn} · ${inc.estado}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { Titulo(stringResource(R.string.geocercas)) }
            items(estado.geocercas, key = { "geo-${it.id}" }) { g ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(g.nombre, fontWeight = FontWeight.SemiBold)
                            Text("${g.oficina} · radio ${g.radioMetros} m", style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(onClick = { viewModel.cambiarRadio(g, (g.radioMetros - 50).coerceAtLeast(50)) }) { Text("−50") }
                        OutlinedButton(onClick = { viewModel.cambiarRadio(g, g.radioMetros + 50) }) { Text("+50") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
}

/** Un hito con color según su resultado (verificado, fuera de zona, atrasado, GPS impreciso). */
@Composable
private fun FilaHito(h: HitoPanel, mostrarCoordenadas: Boolean = false) {
    val color = when (h.resultado) {
        "VERIFICADA", "REGISTRADA" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${h.hora} · ${h.usuario}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Etiqueta("${h.tipo} ${h.resultado}", color)
            }
            Text("Servicio ${h.servicioId} · ${h.lugar ?: ""}" + (h.distanciaMetros?.let { " · ${it.roundToInt()} m" } ?: ""),
                style = MaterialTheme.typography.bodySmall)
            if (h.mensaje.isNotBlank()) Text(h.mensaje, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (mostrarCoordenadas) Text(String.format(Locale.US, "%.5f, %.5f", h.latitud, h.longitud), style = MaterialTheme.typography.bodySmall)
        }
    }
}
