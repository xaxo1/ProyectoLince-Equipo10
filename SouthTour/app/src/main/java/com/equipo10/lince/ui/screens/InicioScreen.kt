package com.equipo10.lince.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.InicioUiState
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ChipDato
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.EtiquetaEstado
import com.equipo10.lince.ui.components.FilaIcono
import com.equipo10.lince.ui.components.TarjetaSouthTour
import com.equipo10.lince.ui.components.colorEstado
import com.equipo10.lince.ui.components.iconoTipo
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.viewmodel.InicioViewModel

/** Pantalla "Hoy": servicios asignados para hoy (diseño "servicios_del_dia" de Stitch). */
@Composable
fun InicioScreen(
    onVerDetalle: (Int) -> Unit,
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
        else -> ContenidoHoy(estado, onVerDetalle)
    }
}

@Composable
private fun ContenidoHoy(estado: InicioUiState, onVerDetalle: (Int) -> Unit) {
    val contexto = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ----- Encabezado con fecha y contadores -----
        item {
            Column {
                Text(
                    estado.fechaHoy.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "Hola, ${estado.nombre.substringBefore(" ")}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val cantidad = estado.serviciosHoy.size
                    ChipDato(Icons.Filled.PinDrop, if (cantidad == 1) "1 servicio" else "$cantidad servicios")
                    ChipDato(Icons.Filled.Groups, "${estado.totalPasajeros} pasajeros")
                    ChipDato(Icons.Filled.Person, estado.rol)
                }
            }
        }

        // ----- Servicios asignados -----
        items(estado.serviciosHoy, key = { it.id }) { servicio ->
            TarjetaHoy(
                servicio = servicio,
                onAccion = { onVerDetalle(servicio.id) },
                onNavegar = { abrirEnMapas(contexto, servicio.puntoPickup) }
            )
        }

        // ----- Fin de la jornada -----
        if (estado.jornadaTerminada) {
            item { TarjetaFinJornada(estado.proximo) }
        }
    }
}

/** Tarjeta de servicio de "Hoy": franja de estado arriba, hora grande y acción principal. */
@Composable
private fun TarjetaHoy(servicio: Servicio, onAccion: () -> Unit, onNavegar: () -> Unit) {
    TarjetaSouthTour(onClick = onAccion) {
        // Franja superior con el color del estado
        Box(Modifier.fillMaxWidth().height(6.dp).background(colorEstado(servicio.estado)))

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("HORA DE RECOGIDA", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(servicio.horaPickup, style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold)
                        Text(" hrs", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp))
                    }
                }
                EtiquetaEstado(servicio.estado)
            }

            // Caja arena con el servicio y el punto de pickup
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilaIcono(iconoTipo(servicio.tipo), servicio.excursion, destacado = true)
                    FilaIcono(Icons.Filled.PinDrop, servicio.puntoPickup)
                }
            }

            // Chips: pasajeros, contraparte y vehículo
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChipDato(Icons.Filled.Groups, "${servicio.pasajeros} pasajeros")
                servicio.vehiculo?.let { ChipDato(Icons.Filled.DirectionsBus, it.substringBefore(" ·")) }
            }

            if (servicio.estado == EstadoServicio.CANCELADO) {
                // Regla del equipo: el cancelado igual se paga al recurso
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(buildAnnotatedString {
                            append("${servicio.motivoCancelacion ?: "Cancelado"} • ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Tarifa 100 % pagada") }
                        }, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // WHEN: el texto y el ícono del botón dependen del estado
                    val (texto, icono) = when (servicio.estado) {
                        EstadoServicio.CONFIRMADO -> "Iniciar check-in" to Icons.Filled.HowToReg
                        EstadoServicio.EN_CURSO -> "Continuar servicio" to Icons.Filled.PlayArrow
                        else -> "Ver registro" to Icons.Filled.Visibility
                    }
                    Button(
                        onClick = onAccion,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(texto, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(10.dp))
                    FilledIconButton(
                        onClick = onNavegar,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.Navigation, contentDescription = "Abrir en mapas")
                    }
                }
            }
        }
    }
}

/** Tarjeta "Fin de la jornada de hoy" con el próximo servicio. */
@Composable
private fun TarjetaFinJornada(proximo: Servicio?) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Bedtime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(10.dp))
            Text("Fin de la jornada de hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                // LET + ELVIS: el texto cambia si existe un próximo servicio
                proximo?.let {
                    "No hay más servicios pendientes para hoy. Tu próximo servicio es el " +
                        "${Formatos.fechaLegible(it.fecha).lowercase()} a las ${it.horaPickup} hrs."
                } ?: "No hay más servicios pendientes para hoy.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            ChipDato(Icons.Filled.Verified, "Bitácora al día")
        }
    }
}

/** Abre el punto de pickup en Google Maps (o en el navegador si no está instalado). */
private fun abrirEnMapas(contexto: Context, lugar: String) {
    val url = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode("$lugar, Santiago, Chile")
    try {
        contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        // No hay app capaz de abrir el enlace: no se hace nada
    }
}
