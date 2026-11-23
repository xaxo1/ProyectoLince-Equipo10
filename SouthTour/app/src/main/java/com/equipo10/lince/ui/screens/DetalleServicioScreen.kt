package com.equipo10.lince.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.R
import com.equipo10.lince.model.DetalleUiState
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.EstadosLince
import com.equipo10.lince.model.Incidencia
import com.equipo10.lince.model.RegistroHito
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.ui.components.BannerMensaje
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.Etiqueta
import com.equipo10.lince.ui.components.EtiquetaEstado
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.viewmodel.DetalleServicioViewModel
import java.util.Locale
import kotlin.math.roundToInt

/**
 * "Detalle del servicio" + check-in / check-out con GEOCERCA.
 * Muestra el radio del hotel, la distancia y la precisión del GPS, y el resultado que devuelve el backend.
 */
@Composable
fun DetalleServicioScreen(
    servicioId: Int,
    onVerReserva: (Int) -> Unit,
    onReportarIncidencia: (Int) -> Unit,
    viewModel: DetalleServicioViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // PERMISO de ubicación en primer plano: se pide la primera vez que se registra un hito
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> viewModel.avanzarEstado() }

    val conPermiso: (() -> Unit) -> Unit = { accion ->
        val tienePermiso = PERMISOS_UBICACION.any {
            ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED
        }
        if (tienePermiso) accion() else lanzadorPermiso.launch(PERMISOS_UBICACION)
    }

    LaunchedEffect(servicioId) { viewModel.cargar(servicioId) }

    val error = estado.error
    val servicio = estado.servicio
    when {
        estado.cargando && servicio == null -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = { viewModel.cargar(servicioId) })
        servicio != null -> ContenidoDetalle(
            estado = estado,
            servicio = servicio,
            onAvanzar = { conPermiso(viewModel::avanzarEstado) },
            onMedir = { conPermiso(viewModel::medirDistancia) },
            onReportarPosicion = { conPermiso(viewModel::reportarPosicion) },
            onResponder = viewModel::responderAsignacion,
            onWaze = { abrirWaze(contexto, estado, servicio) },
            onVerReserva = { onVerReserva(servicio.id) },
            onReportarIncidencia = { onReportarIncidencia(servicio.id) }
        )
    }
}

private val PERMISOS_UBICACION = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContenidoDetalle(
    estado: DetalleUiState,
    servicio: Servicio,
    onAvanzar: () -> Unit,
    onMedir: () -> Unit,
    onReportarPosicion: () -> Unit,
    onResponder: (Boolean) -> Unit,
    onWaze: () -> Unit,
    onVerReserva: () -> Unit,
    onReportarIncidencia: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- Encabezado ----------
        Column {
            Text(servicio.excursion, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("${Formatos.fechaLegible(servicio.fecha)} · pickup ${servicio.horaPickup}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                EtiquetaEstado(servicio.estado)
                Etiqueta(servicio.tipo.etiqueta, MaterialTheme.colorScheme.primary)
                Etiqueta("Lince ${servicio.estadoLince}", MaterialTheme.colorScheme.secondary)
                Etiqueta("PL ${servicio.statusPL ?: "—"}", MaterialTheme.colorScheme.secondary)
                if (servicio.conAtraso) Etiqueta(stringResource(R.string.atraso), MaterialTheme.colorScheme.error)
                if (servicio.sincronizacionPendiente) Etiqueta(stringResource(R.string.pendiente_sync), MaterialTheme.colorScheme.error)
            }
        }

        BannerMensaje(texto = estado.mensaje, esAlerta = estado.mensajeEsAlerta)

        // ---------- Confirmar / rechazar la asignación ----------
        if (servicio.estado == EstadoServicio.CONFIRMADO) {
            Seccion(stringResource(R.string.asignacion)) {
                Text(
                    when (servicio.confirmacion) {
                        "CONFIRMADA" -> stringResource(R.string.asignacion_confirmada)
                        "RECHAZADA" -> stringResource(R.string.asignacion_rechazada)
                        else -> stringResource(R.string.asignacion_pendiente)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onResponder(true) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.confirmar))
                    }
                    OutlinedButton(onClick = { onResponder(false) }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.rechazar))
                    }
                }
            }
        }

        // ---------- Geocerca y check-in / check-out ----------
        Seccion(stringResource(R.string.llegada_geocerca)) {
            val geocerca = estado.geocerca
            FilaDetalle(stringResource(R.string.punto_pickup), servicio.puntoPickup)
            if (geocerca != null) {
                FilaDetalle(stringResource(R.string.radio_geocerca), "${geocerca.radioMetros} m")
                FilaDetalle(stringResource(R.string.centro), String.format(Locale.US, "%.5f, %.5f", geocerca.latitud, geocerca.longitud))
            } else {
                Text(stringResource(R.string.sin_geocerca), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            FilaDetalle(stringResource(R.string.regla), stringResource(R.string.regla_texto, estado.reglas.minutosAntes, estado.reglas.precisionMaxima.roundToInt()))

            // Lectura actual del GPS (calculada en el teléfono con Haversine)
            estado.distanciaActual?.let { d ->
                val precision = estado.ubicacionActual?.precisionMetros ?: 0.0
                val dentro = geocerca != null && d <= geocerca.radioMetros
                Surface(
                    color = (if (dentro) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error).copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.distancia_actual, d.roundToInt(), precision.roundToInt()) +
                            if (dentro) " ✓" else " ✗",
                        modifier = Modifier.padding(10.dp), fontWeight = FontWeight.SemiBold
                    )
                }
            }
            OutlinedButton(onClick = onMedir, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.medir_distancia))
            }

            HorizontalDivider()
            FilaDetalle(stringResource(R.string.check_in), servicio.horaCheckIn ?: stringResource(R.string.pendiente))
            FilaDetalle(stringResource(R.string.check_out), servicio.horaCheckOut ?: stringResource(R.string.pendiente))
            estado.ultimoRegistro?.let { ResultadoServidor(it) }

            val textoBoton: String? = when (servicio.estado) {
                EstadoServicio.CONFIRMADO -> stringResource(R.string.hacer_check_in)
                EstadoServicio.EN_CURSO -> stringResource(R.string.hacer_check_out)
                EstadoServicio.FINALIZADO, EstadoServicio.CANCELADO -> null
            }
            textoBoton?.let { texto ->
                Button(onClick = onAvanzar, enabled = !estado.procesando, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    if (estado.procesando) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(texto, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            // Transportista en servicio activo: reporte PUNTUAL de posición (no rastreo 24/7)
            if (estado.esTransportista && servicio.estado == EstadoServicio.EN_CURSO) {
                TextButton(onClick = onReportarPosicion) {
                    Icon(Icons.Filled.ShareLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.reportar_posicion))
                }
            }
        }

        // ---------- Navegación con Waze ----------
        OutlinedButton(onClick = onWaze, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.ir_con_waze, servicio.puntoPickup))
        }

        // ---------- Incidencias ----------
        Seccion(stringResource(R.string.incidencias_titulo, estado.incidencias.size)) {
            val iniciado = servicio.estado == EstadoServicio.EN_CURSO || servicio.estado == EstadoServicio.FINALIZADO
            if (estado.incidencias.isEmpty()) {
                Text(stringResource(R.string.sin_incidencias), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            estado.incidencias.forEachIndexed { indice, incidencia ->
                if (indice > 0) HorizontalDivider()
                FilaIncidencia(incidencia)
            }
            if (iniciado) {
                OutlinedButton(onClick = onReportarIncidencia, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.ReportProblem, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.reportar_incidencia))
                }
            }
        }

        // ---------- Pickup, dropoff y recursos ----------
        Seccion(stringResource(R.string.pickup_dropoff)) {
            FilaDetalle(stringResource(R.string.hora_pickup), servicio.horaPickup)
            FilaDetalle(stringResource(R.string.punto_pickup), servicio.puntoPickup)
            FilaDetalle(stringResource(R.string.dropoff), servicio.dropOff)
            FilaDetalle(stringResource(R.string.pasajeros), "${servicio.pasajeros} (${servicio.paxTexto})")
            FilaDetalle(stringResource(R.string.oficina), servicio.oficina)
            servicio.vehiculo?.let { FilaDetalle(stringResource(R.string.vehiculo), it) }
            servicio.transportista?.let { FilaDetalle(stringResource(R.string.transportista), it) }
            servicio.guia?.let { FilaDetalle(stringResource(R.string.guia), it) }
            servicio.idiomaRequerido?.let { FilaDetalle(stringResource(R.string.idioma_requerido), it) }
        }

        // ---------- Reserva (Lince API) ----------
        Seccion(stringResource(R.string.reserva)) {
            FilaDetalle(stringResource(R.string.agencia), servicio.agencia)
            FilaDetalle(stringResource(R.string.n_reserva), servicio.numeroReserva)
            FilaDetalle(stringResource(R.string.titular), servicio.titularReserva)
            FilaDetalle(stringResource(R.string.estado_servicio), "${servicio.estadoLince} · ${EstadosLince.servicio(servicio.estadoLince)}")
            FilaDetalle(stringResource(R.string.asignacion), "${servicio.statusPL ?: "—"} · ${EstadosLince.asignacion(servicio.statusPL)}")
            servicio.notas?.let { Text("• $it") }
            TextButton(onClick = onVerReserva) { Text(stringResource(R.string.ver_reserva)) }
        }
    }
}

/** Respuesta del servidor al último hito: resultado, distancia, radio y precisión. */
@Composable
private fun ResultadoServidor(registro: RegistroHito) {
    val ok = registro.verificado
    val color = when {
        registro.pendiente -> MaterialTheme.colorScheme.secondary
        ok -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    Surface(color = color.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${registro.tipo.name.replace('_', '-')} · ${registro.resultado}", fontWeight = FontWeight.Bold, color = color)
            Text(registro.mensaje, style = MaterialTheme.typography.bodySmall)
            val partes = listOfNotNull(
                registro.distanciaMetros?.let { "${it.roundToInt()} m" },
                registro.radioMetros?.let { "radio $it m" },
                "±${registro.precisionMetros.roundToInt()} m GPS",
                registro.hora.takeIf { it.isNotEmpty() }
            )
            Text(partes.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Una incidencia del backend, con la miniatura de su foto si se tomó en este teléfono. */
@Composable
private fun FilaIncidencia(incidencia: Incidencia) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val foto = remember(incidencia.rutaFoto) { incidencia.rutaFoto?.let { BitmapFactory.decodeFile(it) } }
        foto?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Foto de la incidencia",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                incidencia.tipo.etiqueta + if (incidencia.automatica) " (automática)" else "",
                fontWeight = FontWeight.SemiBold,
                color = if (incidencia.automatica) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(incidencia.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text(
                incidencia.registradaEn + (incidencia.pasajerosPresentes?.let { " · $it pax presentes" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Abre Waze navegando al hotel de pickup (coordenadas de la geocerca, o el nombre si no hay). */
private fun abrirWaze(contexto: Context, estado: DetalleUiState, servicio: Servicio) {
    val g = estado.geocerca
    val url = if (g != null) String.format(Locale.US, "https://waze.com/ul?ll=%.6f,%.6f&navigate=yes", g.latitud, g.longitud)
        else "https://waze.com/ul?q=" + Uri.encode(servicio.puntoPickup) + "&navigate=yes"
    try {
        contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        // Sin Waze ni navegador: no se hace nada
    }
}

/** Tarjeta blanca con título; el contenido se pasa como lambda (función de orden superior). */
@Composable
fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

@Composable
fun FilaDetalle(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(valor, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.4f))
    }
}
