package com.equipo10.lince.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.R
import com.equipo10.lince.model.EstadosLince
import com.equipo10.lince.model.ResumenMensual
import com.equipo10.lince.model.ResumenUiState
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.TarjetaServicio
import com.equipo10.lince.viewmodel.ResumenViewModel

/** "Resumen" del mes: mis servicios (Lince API), mi puntualidad (hitos del backend) y el total de Southbound. */
@Composable
fun ResumenScreen(viewModel: ResumenViewModel = viewModel(), onVerDetalle: (Int) -> Unit = {}) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.cargar() }

    val error = estado.error
    val resumen = estado.resumen
    when {
        estado.cargando && resumen == null -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = viewModel::cargar)
        resumen != null -> ContenidoResumen(estado, resumen, viewModel, onVerDetalle)
    }
}

@Composable
private fun ContenidoResumen(estado: ResumenUiState, r: ResumenMensual, vm: ResumenViewModel, onVerDetalle: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = vm::mesAnterior) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior") }
                Text(r.mes, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                IconButton(onClick = vm::mesSiguiente, enabled = estado.desplazamientoMes < 0) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
                }
            }
            if (!r.esMesActual) TextButton(onClick = vm::volverAlMesActual) { Text(stringResource(R.string.mes_actual)) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Cifra(stringResource(R.string.mis_servicios), "${r.misServicios}", Modifier.weight(1f))
                Cifra(stringResource(R.string.completados), "${r.completados}", Modifier.weight(1f))
                Cifra(stringResource(R.string.cancelados), "${r.cancelados}", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Cifra(stringResource(R.string.puntualidad), r.puntualidad?.let { "$it %" } ?: "—", Modifier.weight(1f))
                Cifra(stringResource(R.string.total_southbound), "${r.totalSouthbound}", Modifier.weight(1f))
            }
        }
        item {
            Seccion(stringResource(R.string.por_tipo)) {
                r.porTipo.forEach { (tipo, n) -> FilaDetalle(tipo.etiqueta, "$n") }
                if (r.porTipo.isEmpty()) Text("—")
            }
        }
        item {
            Seccion(stringResource(R.string.southbound_por_estado)) {
                r.porEstadoSouthbound.forEach { (codigo, n) -> FilaDetalle("$codigo · ${EstadosLince.servicio(codigo)}", "$n") }
            }
        }
        items(r.detalle, key = { it.id }) { s -> TarjetaServicio(servicio = s, onClick = { onVerDetalle(s.id) }) }
    }
}

@Composable
private fun Cifra(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Text(titulo, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}
