package com.equipo10.lince.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.equipo10.lince.model.DetalleUiState
import com.equipo10.lince.model.EstadoServicio
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
    viewModel: DetalleServicioViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

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
            onAvanzar = viewModel::avanzarEstado,
            onVerReserva = { onVerReserva(servicio.numeroReserva) }
        )
    }
}

@Composable
private fun ContenidoDetalle(
    estado: DetalleUiState,
    servicio: Servicio,
    onAvanzar: () -> Unit,
    onVerReserva: () -> Unit
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
            FilaDetalle("Check-out", servicio.horaCheckOut ?: "Pendiente")
            Text(
                "La ubicación GPS se registrará en una próxima entrega.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
