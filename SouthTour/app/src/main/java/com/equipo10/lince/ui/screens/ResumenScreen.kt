package com.equipo10.lince.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.ResumenMensual
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ChipDato
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.Etiqueta
import com.equipo10.lince.ui.components.TarjetaSouthTour
import com.equipo10.lince.ui.theme.ArenaSouthTour
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.viewmodel.ResumenViewModel

/** Pantalla "Resumen" mensual (diseño "resumen_mensual" de Stitch + puntualidad). */
@Composable
fun ResumenScreen(viewModel: ResumenViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargar() }

    val error = estado.error
    val resumen = estado.resumen
    when {
        estado.cargando && resumen == null -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = viewModel::cargar)
        resumen != null -> ContenidoResumen(
            resumen = resumen,
            onAnterior = viewModel::mesAnterior,
            onSiguiente = viewModel::mesSiguiente,
            onMesActual = viewModel::volverAlMesActual
        )
    }
}

@Composable
private fun ContenidoResumen(
    resumen: ResumenMensual,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onMesActual: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ----- Selector de mes -----
        item {
            TarjetaSouthTour {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAnterior) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(resumen.mes, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("PERÍODO CONTABLE", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onSiguiente, enabled = !resumen.esMesActual) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
                    }
                }
            }
        }
        item {
            if (resumen.esMesActual) {
                ChipDato(Icons.Filled.CheckCircle, "Mes actual")
            } else {
                TextButton(onClick = onMesActual) { Text("Volver al mes actual") }
            }
        }

        // ----- Total acumulado (tarjeta café) -----
        item {
            Surface(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()) {
                Column {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("TOTAL ACUMULADO DEL MES", style = MaterialTheme.typography.labelMedium,
                                color = ArenaSouthTour, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = ArenaSouthTour)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(Formatos.pesos(resumen.montoTotal), style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Pesos chilenos (CLP)", color = Color.White.copy(alpha = 0.8f))
                    }
                    Row(
                        Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.15f)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Event, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Pago estimado: ${resumen.pagoEstimado}", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ----- Completados y cancelados pagados -----
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaIndicador(
                    titulo = "Servicios completados",
                    valor = "${resumen.completados}",
                    icono = Icons.Filled.Verified,
                    color = MaterialTheme.colorScheme.tertiary,
                    // La puntualidad es lo que el cliente quiere premiar
                    pie = resumen.puntualidad?.let { "Puntualidad $it %" } ?: "Sin check-in",
                    modifier = Modifier.weight(1f)
                )
                TarjetaIndicador(
                    titulo = "Cancelados (pagados)",
                    valor = "${resumen.cancelados}",
                    icono = Icons.Filled.Payments,
                    color = MaterialTheme.colorScheme.secondary,
                    pie = "100 % abonado",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ----- Detalle de servicios -----
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Detalle de servicios", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                ChipDato(Icons.Filled.Schedule, "${resumen.detalle.size} registros")
            }
        }
        if (resumen.detalle.isEmpty()) {
            item {
                Text("No hay servicios cerrados en este mes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(resumen.detalle, key = { it.id }) { servicio -> FilaServicioResumen(servicio) }
    }
}

@Composable
private fun TarjetaIndicador(
    titulo: String,
    valor: String,
    icono: ImageVector,
    color: Color,
    pie: String,
    modifier: Modifier = Modifier
) {
    TarjetaSouthTour(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(titulo, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Box(Modifier.size(36.dp).background(color.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Text(valor, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Etiqueta(pie, color)
        }
    }
}

@Composable
private fun FilaServicioResumen(servicio: Servicio) {
    val cancelado = servicio.estado == EstadoServicio.CANCELADO
    TarjetaSouthTour {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text("${Formatos.diaMes(servicio.fecha)} • ${servicio.horaPickup}",
                        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(servicio.excursion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (cancelado) "${servicio.motivoCancelacion ?: "Cancelado"} • Cobro al 100 %"
                    else servicio.vehiculo ?: servicio.agencia,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (cancelado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (cancelado) Etiqueta("Cancelado pagado", MaterialTheme.colorScheme.secondary)
                else Etiqueta("Completado", MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.height(6.dp))
                Text(Formatos.pesos(servicio.monto), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("CLP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
