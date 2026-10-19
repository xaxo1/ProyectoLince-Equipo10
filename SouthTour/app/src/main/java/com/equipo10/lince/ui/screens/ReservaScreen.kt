package com.equipo10.lince.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.model.Reserva
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.viewmodel.ReservaViewModel

/** "Reserva completa": itinerario de los pasajeros por número de reserva. */
@Composable
fun ReservaScreen(
    numeroReserva: String,
    viewModel: ReservaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(numeroReserva) { viewModel.cargar(numeroReserva) }

    val error = estado.error
    val reserva = estado.reserva
    when {
        estado.cargando -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = { viewModel.cargar(numeroReserva) })
        reserva != null -> ContenidoReserva(reserva)
    }
}

@Composable
private fun ContenidoReserva(reserva: Reserva) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text("Reserva ${reserva.numero}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(reserva.agencia, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Seccion("Pasajeros") {
            FilaDetalle("Titular", reserva.titular)
            FilaDetalle("Nacionalidad", reserva.nacionalidad)
            FilaDetalle("Total de pasajeros", "${reserva.totalPasajeros}")
        }

        Seccion("Contacto de la agencia") {
            Text(reserva.contactoAgencia)
        }

        if (reserva.observaciones.isNotEmpty()) {
            Seccion("Observaciones") {
                reserva.observaciones.forEach { observacion -> Text("• $observacion") }
            }
        }

        Seccion("Itinerario completo") {
            // forEachIndexed: recorre la lista sabiendo la posición (para no poner línea al final)
            reserva.itinerario.forEachIndexed { indice, actividad ->
                Row {
                    Column(Modifier.width(96.dp)) {
                        Text(
                            Formatos.fechaCorta(actividad.fecha),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            actividad.hora,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(actividad.descripcion, modifier = Modifier.weight(1f))
                }
                if (indice < reserva.itinerario.lastIndex) HorizontalDivider()
            }
        }
    }
}
