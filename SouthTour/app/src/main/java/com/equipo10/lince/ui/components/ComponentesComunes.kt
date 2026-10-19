package com.equipo10.lince.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.Servicio

/** Indicador de carga centrado (se reutiliza en todas las pantallas). */
@Composable
fun CargandoContenido(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Mensaje de error con botón para reintentar. */
@Composable
fun ErrorContenido(mensaje: String, onReintentar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReintentar) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

/** Banner con el mensaje del UiState: turquesa si es informativo, terracota si es alerta. */
@Composable
fun BannerMensaje(texto: String, esAlerta: Boolean, modifier: Modifier = Modifier) {
    val color = if (esAlerta) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (esAlerta) Icons.Filled.Warning else Icons.Filled.Info,
                contentDescription = null,
                tint = color
            )
            Spacer(Modifier.width(10.dp))
            Text(texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** WHEN como expresión: cada estado tiene su color de la paleta SouthTour. */
@Composable
fun colorEstado(estado: EstadoServicio): Color = when (estado) {
    EstadoServicio.CONFIRMADO -> MaterialTheme.colorScheme.tertiary        // verde confirmado
    EstadoServicio.EN_CURSO -> MaterialTheme.colorScheme.secondary         // turquesa
    EstadoServicio.FINALIZADO -> MaterialTheme.colorScheme.onSurfaceVariant
    EstadoServicio.CANCELADO -> MaterialTheme.colorScheme.error            // alerta
}

/** "Pastilla" de color genérica. */
@Composable
fun Etiqueta(texto: String, color: Color) {
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(
            text = texto,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Pastilla con el estado del servicio. */
@Composable
fun EtiquetaEstado(estado: EstadoServicio) {
    Etiqueta(estado.etiqueta, colorEstado(estado))
}

/**
 * Tarjeta de servicio reutilizada en "Servicios del día" y en el "Calendario".
 * La hora de pickup va destacada en turquesa (color secundario), como indica la paleta.
 */
@Composable
fun TarjetaServicio(servicio: Servicio, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            // WITH: dentro del bloque se usan las propiedades sin escribir "servicio."
            with(servicio) {
                Column(Modifier.width(64.dp)) {
                    Text(
                        horaPickup,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "pickup",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(excursion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            puntoPickup, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$pasajeros pasajeros · $agencia", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EtiquetaEstado(estado)
                        if (conAtraso) Etiqueta("Atraso", MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
