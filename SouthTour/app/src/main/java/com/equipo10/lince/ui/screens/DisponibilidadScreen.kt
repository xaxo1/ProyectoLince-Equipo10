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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.model.DiaDisponibilidad
import com.equipo10.lince.model.DisponibilidadUiState
import com.equipo10.lince.model.EstadoDia
import com.equipo10.lince.ui.components.BannerMensaje
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.Etiqueta
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.viewmodel.DisponibilidadViewModel

/** "Disponibilidad": bloqueo de días con validación de una semana de anticipación. */
@Composable
fun DisponibilidadScreen(viewModel: DisponibilidadViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargar() }

    val error = estado.error
    when {
        estado.cargando -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = viewModel::cargar)
        else -> ListaDias(
            estado = estado,
            onBloquear = viewModel::bloquear,
            onDesbloquear = viewModel::desbloquear
        )
    }
}

@Composable
private fun ListaDias(
    estado: DisponibilidadUiState,
    onBloquear: (String) -> Unit,
    onDesbloquear: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Días bloqueados: ${estado.bloqueados}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                BannerMensaje(texto = estado.mensaje, esAlerta = estado.mensajeEsAlerta)
            }
        }

        items(estado.dias, key = { it.fecha }) { dia ->
            FilaDia(dia = dia, onBloquear = { onBloquear(dia.fecha) }, onDesbloquear = { onDesbloquear(dia.fecha) })
        }
    }
}

@Composable
private fun FilaDia(dia: DiaDisponibilidad, onBloquear: () -> Unit, onDesbloquear: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(Formatos.fechaLegible(dia.fecha), fontWeight = FontWeight.Medium)
                Text(
                    // WHEN sin argumento: decide el texto según condiciones
                    text = when {
                        dia.diasDeAnticipacion == 0 -> "Hoy"
                        dia.diasDeAnticipacion == 1 -> "Mañana"
                        else -> "En ${dia.diasDeAnticipacion} días"
                    } + (dia.servicio?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (dia.estado) {
                EstadoDia.CON_SERVICIO -> Etiqueta(dia.estado.etiqueta, MaterialTheme.colorScheme.tertiary)
                EstadoDia.BLOQUEADO -> Column(horizontalAlignment = Alignment.End) {
                    Etiqueta(dia.estado.etiqueta, MaterialTheme.colorScheme.error)
                    TextButton(onClick = onDesbloquear) { Text("Desbloquear") }
                }
                // El botón está siempre activo: si no cumple la regla, el ViewModel muestra el error
                EstadoDia.LIBRE -> OutlinedButton(onClick = onBloquear) { Text("Bloquear") }
            }
        }
    }
}
