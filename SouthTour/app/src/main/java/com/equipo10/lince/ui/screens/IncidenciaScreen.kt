package com.equipo10.lince.ui.screens

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.model.IncidenciaUiState
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.model.TipoIncidencia
import com.equipo10.lince.ui.components.BannerMensaje
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.viewmodel.IncidenciaViewModel

/**
 * Formulario "Reportar incidencia" (se abre desde el Detalle de un servicio iniciado).
 * Campos obligatorios con mensaje de error propio, foto opcional con la CÁMARA
 * y confirmación al guardar en Room.
 */
@Composable
fun IncidenciaScreen(
    servicioId: Int,
    onListo: () -> Unit,
    viewModel: IncidenciaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(servicioId) { viewModel.cargar(servicioId) }

    // RECURSO NATIVO: cámara. TakePicturePreview abre la app de cámara y devuelve una miniatura (Bitmap).
    // No necesita el permiso CAMERA porque la foto la toma la app de cámara del sistema.
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto ->
        viewModel.onFotoTomada(foto)
    }
    val abrirCamara = {
        try {
            lanzadorCamara.launch(null)
        } catch (e: ActivityNotFoundException) {
            viewModel.mostrarAviso("Este dispositivo no tiene una app de cámara disponible")
        }
    }

    val error = estado.error
    val servicio = estado.servicio
    when {
        estado.cargando -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = { viewModel.cargar(servicioId) })
        servicio != null -> FormularioIncidencia(
            estado = estado,
            servicio = servicio,
            onTipo = viewModel::onTipoChange,
            onDescripcion = viewModel::onDescripcionChange,
            onPasajeros = viewModel::onPasajerosChange,
            onTomarFoto = abrirCamara,
            onQuitarFoto = viewModel::quitarFoto,
            onGuardar = viewModel::guardar
        )
    }

    // Confirmación: se muestra cuando el ViewModel avisa que se guardó
    estado.guardada?.let { incidencia ->
        AlertDialog(
            onDismissRequest = onListo,
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
            title = { Text("Incidencia registrada") },
            text = {
                Text(
                    "Se guardó \"${incidencia.tipo.etiqueta}\" a las ${incidencia.registradaEn.substringAfter(" ")}" +
                        (if (incidencia.rutaFoto != null) " con foto" else "") +
                        ". Queda en el teléfono aunque cierres la app."
                )
            },
            confirmButton = { TextButton(onClick = onListo) { Text("Volver al servicio") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormularioIncidencia(
    estado: IncidenciaUiState,
    servicio: Servicio,
    onTipo: (TipoIncidencia) -> Unit,
    onDescripcion: (String) -> Unit,
    onPasajeros: (String) -> Unit,
    onTomarFoto: () -> Unit,
    onQuitarFoto: () -> Unit,
    onGuardar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(servicio.excursion, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Pickup ${servicio.horaPickup} · ${servicio.pasajeros} pasajeros · Reserva ${servicio.numeroReserva}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        estado.aviso?.let { BannerMensaje(texto = it, esAlerta = true) }

        // ---------- Tipo (obligatorio) ----------
        Seccion("Tipo de incidencia *") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoIncidencia.entries.forEach { tipo ->
                    FilterChip(
                        selected = estado.tipo == tipo,
                        onClick = { onTipo(tipo) },
                        label = { Text(tipo.etiqueta) }
                    )
                }
            }
            estado.errorTipo?.let { TextoError(it) }
        }

        // ---------- Descripción y pasajeros (obligatorios) ----------
        Seccion("Detalle *") {
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = onDescripcion,
                label = { Text("Descripción") },
                placeholder = { Text("¿Qué ocurrió?") },
                isError = estado.errorDescripcion != null,
                supportingText = {
                    Row {
                        Text(estado.errorDescripcion ?: "Mínimo ${IncidenciaViewModel.MIN_DESCRIPCION} caracteres",
                            modifier = Modifier.weight(1f))
                        Text("${estado.descripcion.length}/${IncidenciaViewModel.MAX_DESCRIPCION}")
                    }
                },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.pasajerosPresentes,
                onValueChange = onPasajeros,
                label = { Text("Pasajeros presentes") },
                isError = estado.errorPasajeros != null,
                supportingText = {
                    Text(estado.errorPasajeros ?: "De ${servicio.pasajeros} pasajeros en la reserva")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ---------- Foto (opcional) ----------
        Seccion("Foto de evidencia (opcional)") {
            val foto = estado.foto
            if (foto != null) {
                Image(
                    bitmap = foto.asImageBitmap(),
                    contentDescription = "Foto de la incidencia",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(MaterialTheme.shapes.medium)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onTomarFoto) { Text("Tomar otra") }
                    TextButton(onClick = onQuitarFoto) { Text("Quitar foto") }
                }
            } else {
                OutlinedButton(onClick = onTomarFoto, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tomar foto con la cámara")
                }
            }
        }

        Button(
            onClick = onGuardar,
            enabled = !estado.guardando,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (estado.guardando) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Guardar incidencia")
            }
        }
        Text(
            "* Campos obligatorios",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun TextoError(texto: String) {
    Text(texto, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}
