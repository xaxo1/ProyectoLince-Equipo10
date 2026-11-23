package com.equipo10.lince.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.R
import com.equipo10.lince.model.FiltroServicios
import com.equipo10.lince.model.ServiciosUiState
import com.equipo10.lince.model.TipoServicio
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.TarjetaServicio
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.viewmodel.ServiciosViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** "Calendario de servicios": calendario mensual en cuadrícula + filtros por estado, tipo y texto. */
@Composable
fun ServiciosScreen(
    onVerDetalle: (Int) -> Unit,
    viewModel: ServiciosViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.cargar() }

    val error = estado.error
    when {
        estado.cargando -> CargandoContenido()
        error != null -> ErrorContenido(error, onReintentar = viewModel::cargar)
        else -> Calendario(estado, viewModel, onVerDetalle)
    }
}

@Composable
private fun Calendario(estado: ServiciosUiState, vm: ServiciosViewModel, onVerDetalle: (Int) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // ----- Calendario mensual (cuadrícula) -----
        item {
            CuadriculaMes(
                diasConServicio = estado.diasConServicio,
                seleccionada = estado.fechaSeleccionada,
                onSeleccionar = vm::seleccionarFecha
            )
        }

        // ----- Buscador compacto -----
        item {
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = vm::cambiarBusqueda,
                placeholder = { Text(stringResource(R.string.buscar), style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (estado.busqueda.isNotEmpty()) {
                        IconButton(onClick = { vm.cambiarBusqueda("") }) { Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda") }
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }

        // ----- Filtros por estado y por tipo -----
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroServicios.entries.forEach { f ->
                        FilterChip(selected = estado.filtro == f, onClick = { vm.cambiarFiltro(f) }, label = { Text(f.etiqueta) })
                    }
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoServicio.entries.forEach { t ->
                        FilterChip(selected = estado.filtroTipo == t, onClick = { vm.cambiarTipo(t) }, label = { Text(t.etiqueta) })
                    }
                }
                Text(
                    stringResource(R.string.mostrando_de, estado.cantidad, estado.total) +
                        (estado.fechaSeleccionada?.let { " · " + Formatos.fechaLegible(it) } ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        if (estado.porFecha.isEmpty()) {
            item {
                Text(
                    if (estado.busqueda.isBlank()) stringResource(R.string.sin_servicios_categoria)
                    else stringResource(R.string.sin_coincidencias, estado.busqueda.trim()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        // Recorremos el MAP fecha -> servicios: un título por día y sus tarjetas debajo
        estado.porFecha.forEach { (fecha, serviciosDelDia) ->
            item(key = "fecha-$fecha") {
                Text(Formatos.fechaLegible(fecha), style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp))
            }
            items(serviciosDelDia, key = { it.id }) { servicio ->
                TarjetaServicio(servicio = servicio, onClick = { onVerDetalle(servicio.id) },
                    modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

/**
 * Calendario mensual típico de cuadros (7 columnas, lunes a domingo). Los días con servicio
 * llevan un punto; tocar un día filtra la lista por esa fecha.
 */
@Composable
private fun CuadriculaMes(diasConServicio: Set<String>, seleccionada: String?, onSeleccionar: (String) -> Unit) {
    var desplazamiento by remember { mutableIntStateOf(0) }
    val formatoIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val mes = remember(desplazamiento) {
        Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); add(Calendar.MONTH, desplazamiento) }
    }
    val diasDelMes = mes.getActualMaximum(Calendar.DAY_OF_MONTH)
    // Calendar: domingo = 1 ... sábado = 7 -> columnas con el lunes primero
    val huecosIniciales = (mes.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val hoy = Formatos.hoy()

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { desplazamiento-- }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior") }
            Text(Formatos.nombreMes(desplazamiento), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            IconButton(onClick = { desplazamiento++ }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente") }
        }
        Row {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach {
                Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        val celdas = huecosIniciales + diasDelMes
        val semanas = (celdas + 6) / 7
        for (semana in 0 until semanas) {
            Row {
                for (columna in 0 until 7) {
                    val dia = semana * 7 + columna - huecosIniciales + 1
                    if (dia < 1 || dia > diasDelMes) {
                        Spacer(Modifier.weight(1f).aspectRatio(1.1f))
                    } else {
                        val fecha = formatoIso.format((mes.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dia) }.time)
                        DiaCalendario(
                            numero = dia,
                            conServicio = fecha in diasConServicio,
                            esHoy = fecha == hoy,
                            seleccionado = fecha == seleccionada,
                            onClick = { onSeleccionar(fecha) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaCalendario(numero: Int, conServicio: Boolean, esHoy: Boolean, seleccionado: Boolean,
                          onClick: () -> Unit, modifier: Modifier) {
    val fondo = when {
        seleccionado -> MaterialTheme.colorScheme.secondary
        conServicio -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    Box(
        modifier = modifier
            .aspectRatio(1.1f)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(fondo)
            .then(if (esHoy) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$numero", style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (conServicio || esHoy) FontWeight.Bold else FontWeight.Normal,
                color = if (seleccionado) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface)
            if (conServicio) {
                Spacer(Modifier.height(2.dp))
                Box(Modifier.size(5.dp).background(
                    if (seleccionado) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.secondary, CircleShape))
            }
        }
    }
}
