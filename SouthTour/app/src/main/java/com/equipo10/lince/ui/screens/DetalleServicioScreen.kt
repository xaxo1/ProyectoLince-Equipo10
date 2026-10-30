package com.equipo10.lince.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.model.DetalleUiState
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.Incidencia
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.ui.components.BannerMensaje
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.Etiqueta
import com.equipo10.lince.ui.components.EtiquetaEstado
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.viewmodel.DetalleServicioViewModel

/**
 * "Detalle del servicio" + "Check-in / Check-out".
 * Aplica la Clase 6: la pantalla observa el StateFlow y los botones llaman a
 * avanzarEstado() del ViewModel (el check-in no se puede deshacer: requisito de trazabilidad).
 */
@Composable
fun DetalleServicioScreen(
    servicioId: Int,
    onVerReserva: (String) -> Unit,
    onReportarIncidencia: (Int) -> Unit,
    viewModel: DetalleServicioViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // PERMISO de ubicación en tiempo de ejecución: se pide la primera vez que se hace check-in.
    // Con o sin permiso se registra el check-in; sin permiso queda sin coordenadas (y se avisa).
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> viewModel.avanzarEstado() }

    val onAvanzar = {
        val tienePermiso = PERMISOS_UBICACION.any {
            ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED
        }
        if (tienePermiso) viewModel.avanzarEstado() else lanzadorPermiso.launch(PERMISOS_UBICACION)
    }

    // El id llega como argumento de la ruta "detalle/{servicioId}"
    LaunchedEffect(servicioId) { viewModel.cargar(servicioId) }

    val error = estado.error
    val servicio = estado.servicio
    when {
        estado.cargando -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = { viewModel.cargar(servicioId) })
        servicio != null -> ContenidoDetalle(
            estado = estado,
            servicio = servicio,
            onAvanzar = onAvanzar,
            onVerReserva = { onVerReserva(servicio.numeroReserva) },
            onReportarIncidencia = { onReportarIncidencia(servicio.id) }
        )
    }
}

private val PERMISOS_UBICACION = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
private fun ContenidoDetalle(
    estado: DetalleUiState,
    servicio: Servicio,
    onAvanzar: () -> Unit,
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
            Text(servicio.excursion, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(Formatos.fechaLegible(servicio.fecha), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EtiquetaEstado(servicio.estado)
                if (servicio.conAtraso) Etiqueta("Atraso", MaterialTheme.colorScheme.error)
            }
        }

        // ---------- Mensaje del UiState (como el "mensaje" de la Clase 6) ----------
        BannerMensaje(texto = estado.mensaje, esAlerta = estado.mensajeEsAlerta)

        // ---------- Check-in / Check-out ----------
        Seccion("Check-in / Check-out") {
            FilaDetalle("Check-in", servicio.horaCheckIn ?: "Pendiente")
            servicio.ubicacionCheckIn?.let { FilaDetalle("Ubicación check-in", it) }
            FilaDetalle("Check-out", servicio.horaCheckOut ?: "Pendiente")
            servicio.ubicacionCheckOut?.let { FilaDetalle("Ubicación check-out", it) }
            if (servicio.estado == EstadoServicio.CONFIRMADO) {
                Text(
                    "Al iniciar se registrará la hora y tu ubicación GPS.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // El texto del botón cambia según el estado (WHEN)
            val textoBoton: String? = when (servicio.estado) {
                EstadoServicio.CONFIRMADO -> "Iniciar servicio (check-in)"
                EstadoServicio.EN_CURSO -> "Finalizar servicio (check-out)"
                EstadoServicio.FINALIZADO, EstadoServicio.CANCELADO -> null
            }

            // LET: el botón principal solo existe si hay una acción posible
            textoBoton?.let { texto ->
                Button(
                    onClick = onAvanzar,
                    enabled = !estado.procesando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.procesando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(texto)
                    }
                }
            }
            // Sin botón "Reiniciar": un check-in/check-out registrado no se puede modificar (trazabilidad)
        }

        // ---------- Incidencias (Room + cámara) ----------
        Seccion("Incidencias (${estado.incidencias.size})") {
            val iniciado = servicio.estado == EstadoServicio.EN_CURSO || servicio.estado == EstadoServicio.FINALIZADO
            if (estado.incidencias.isEmpty()) {
                Text("Sin incidencias reportadas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            estado.incidencias.forEachIndexed { indice, incidencia ->
                if (indice > 0) HorizontalDivider()
                FilaIncidencia(incidencia)
            }
            if (iniciado) {
                OutlinedButton(onClick = onReportarIncidencia, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.ReportProblem, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reportar incidencia")
                }
            } else if (servicio.estado == EstadoServicio.CONFIRMADO) {
                Text(
                    "Podrás reportar incidencias después del check-in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ---------- Pickup y drop off ----------
        Seccion("Pickup y drop off") {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    servicio.horaPickup,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary     // hora de pickup destacada
                )
                Spacer(Modifier.width(8.dp))
                Text("hora de pickup", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilaDetalle("Punto de pickup", servicio.puntoPickup)
            FilaDetalle("Drop off", servicio.dropOff)
            FilaDetalle("Pasajeros", "${servicio.pasajeros}")
        }

        // ---------- Reserva ----------
        Seccion("Reserva") {
            FilaDetalle("Agencia", servicio.agencia)
            FilaDetalle("N° de reserva", servicio.numeroReserva)
            FilaDetalle("Pago del servicio", Formatos.pesos(servicio.monto))
            TextButton(onClick = onVerReserva) {
                Text("Ver reserva completa →")
            }
        }

        // ---------- Contraparte ----------
        Seccion("Contraparte") {
            // ELVIS: la contraparte puede no estar asignada todavía
            Text(servicio.contraparte ?: "Aún no hay contraparte asignada")
        }

        // ---------- Requisitos ----------
        Seccion("Requisitos de la agencia") {
            if (servicio.requisitos.isEmpty()) {
                Text("Sin requisitos especiales", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                servicio.requisitos.forEach { requisito -> Text("• $requisito") }
            }
        }
    }
}

/** Una incidencia guardada en Room, con la miniatura de su foto si tiene. */
@Composable
private fun FilaIncidencia(incidencia: Incidencia) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // La foto se lee del archivo guardado; remember evita decodificarla en cada recomposición
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
            Text(incidencia.tipo.etiqueta, fontWeight = FontWeight.SemiBold)
            Text(incidencia.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${Formatos.fechaCorta(incidencia.registradaEn.substringBefore(" "))} " +
                    "${incidencia.registradaEn.substringAfter(" ")} · ${incidencia.pasajerosPresentes} pasajeros presentes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
    Row(Modifier.fillMaxWidth()) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(valor, fontWeight = FontWeight.Medium)
    }
}
