package com.equipo10.lince.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tour
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.equipo10.lince.R
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.Rol
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.model.TipoServicio
import com.equipo10.lince.model.Usuario
import com.equipo10.lince.ui.theme.ArenaSouthTour

// =====================================================================
//  Estados de carga y error
// =====================================================================

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

// =====================================================================
//  Tarjetas, pastillas y chips del sistema "Atacama Fieldwork"
// =====================================================================

/** Tarjeta blanca con borde arena de 1 dp y esquinas de 16 dp (estilo Stitch). */
@Composable
fun TarjetaSouthTour(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    val borde = BorderStroke(1.dp, ArenaSouthTour.copy(alpha = 0.5f))
    // LET: si hay onClick la tarjeta es "clickeable"; si no, es una tarjeta normal
    onClick?.let { accion ->
        Card(onClick = accion, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
            colors = colores, border = borde, content = contenido)
    } ?: Card(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        colors = colores, border = borde, content = contenido)
}

/** WHEN como expresión: cada estado tiene su color de la paleta SouthTour. */
@Composable
fun colorEstado(estado: EstadoServicio): Color = when (estado) {
    EstadoServicio.CONFIRMADO -> MaterialTheme.colorScheme.tertiary        // verde oliva
    EstadoServicio.EN_CURSO -> MaterialTheme.colorScheme.secondary         // turquesa
    EstadoServicio.FINALIZADO -> MaterialTheme.colorScheme.onSurfaceVariant
    EstadoServicio.CANCELADO -> MaterialTheme.colorScheme.error            // terracota
}

fun iconoEstado(estado: EstadoServicio): ImageVector = when (estado) {
    EstadoServicio.CONFIRMADO -> Icons.Filled.CheckCircle
    EstadoServicio.EN_CURSO -> Icons.Filled.PlayArrow
    EstadoServicio.FINALIZADO -> Icons.Filled.Done
    EstadoServicio.CANCELADO -> Icons.Filled.Cancel
}

fun iconoTipo(tipo: TipoServicio): ImageVector = when (tipo) {
    TipoServicio.TRASLADO -> Icons.Filled.FlightLand
    TipoServicio.TOUR -> Icons.Filled.Tour
}

/** Pastilla de color genérica (fondo sólido y texto blanco, como en Stitch). */
@Composable
fun Etiqueta(texto: String, color: Color, icono: ImageVector? = null) {
    Surface(color = color, shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icono?.let {
                Icon(it, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(texto, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Pastilla con el estado del servicio. */
@Composable
fun EtiquetaEstado(estado: EstadoServicio) {
    Etiqueta(estado.etiqueta, colorEstado(estado), iconoEstado(estado))
}

/** Chip de dato con ícono (pasajeros, vehículo, idioma...). */
@Composable
fun ChipDato(icono: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50), modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
            Text(texto, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Fila con ícono y texto (dentro de las cajas color arena de las tarjetas). */
@Composable
fun FilaIcono(icono: ImageVector, texto: String, destacado: Boolean = false) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp).padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(
            texto,
            style = if (destacado) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/** Avatar del usuario: foto para la guía de demostración, iniciales para el resto. */
@Composable
fun Avatar(usuario: Usuario?, tamano: Dp = 36.dp) {
    if (usuario?.rol == Rol.GUIA) {
        Image(
            painter = painterResource(R.drawable.avatar_guia),
            contentDescription = "Perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(tamano).clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier.size(tamano).clip(CircleShape).background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                usuario?.iniciales ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = if (tamano > 48.dp) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Tarjeta de servicio para listas (Calendario).
 * Barra vertical de 6 dp con el color del estado y la hora de pickup en turquesa.
 */
@Composable
fun TarjetaServicio(servicio: Servicio, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TarjetaSouthTour(modifier = modifier, onClick = onClick) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(colorEstado(servicio.estado)))
            // WITH: dentro del bloque se usan las propiedades sin escribir "servicio."
            with(servicio) {
                Column(Modifier.padding(16.dp).weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            horaPickup,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(" hrs", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        EtiquetaEstado(estado)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(excursion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    FilaIcono(Icons.Filled.PinDrop, puntoPickup)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ChipDato(Icons.Filled.Groups, "$pasajeros pax")
                        vehiculo?.let { ChipDato(Icons.Filled.DirectionsBus, it.substringBefore(" ·")) }
                        if (conAtraso) Etiqueta("Atraso", MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
