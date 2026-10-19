package com.equipo10.lince.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.model.InicioUiState
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.TarjetaServicio
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.viewmodel.InicioViewModel

/** Pantalla "Servicios del día" (Inicio): lo asignado para hoy con hora y punto de pickup. */
@Composable
fun InicioScreen(
    onVerDetalle: (Int) -> Unit,
    onVerCalendario: () -> Unit,
    onVerDisponibilidad: () -> Unit,
    viewModel: InicioViewModel = viewModel()
) {
    // Clase 6: el StateFlow se transforma en estado observable por Compose
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    // Recarga cada vez que se entra (así se ven los check-in / check-out recién hechos)
    LaunchedEffect(Unit) { viewModel.cargar() }

    val error = estado.error
    when {
        estado.cargando -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = viewModel::cargar)
        else -> ContenidoInicio(estado, onVerDetalle, onVerCalendario, onVerDisponibilidad)
    }
}

@Composable
private fun ContenidoInicio(
    estado: InicioUiState,
    onVerDetalle: (Int) -> Unit,
    onVerCalendario: () -> Unit,
    onVerDisponibilidad: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    "Hola, ${estado.nombre.substringBefore(" ")}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("${estado.rol} · ${estado.fechaHoy}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            val cantidad = estado.serviciosHoy.size
            Text(
                if (cantidad == 1) "Tienes 1 servicio hoy" else "Tienes $cantidad servicios hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (estado.serviciosHoy.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        "No tienes servicios asignados para hoy.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(estado.serviciosHoy, key = { it.id }) { servicio ->
                TarjetaServicio(servicio = servicio, onClick = { onVerDetalle(servicio.id) })
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text("Próximo servicio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            val proximo = estado.proximo
            if (proximo != null) {
                Column {
                    Text(
                        Formatos.fechaLegible(proximo.fecha),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    TarjetaServicio(servicio = proximo, onClick = { onVerDetalle(proximo.id) })
                }
            } else {
                Text("No tienes más servicios programados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onVerCalendario, modifier = Modifier.weight(1f)) {
                    Text("Calendario")
                }
                OutlinedButton(onClick = onVerDisponibilidad, modifier = Modifier.weight(1f)) {
                    Text("Disponibilidad")
                }
            }
        }
    }
}
