package com.equipo10.lince.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.model.FiltroServicios
import com.equipo10.lince.model.ServiciosUiState
import com.equipo10.lince.ui.components.CargandoContenido
import com.equipo10.lince.ui.components.ErrorContenido
import com.equipo10.lince.ui.components.TarjetaServicio
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.viewmodel.ServiciosViewModel

/** Pantalla "Calendario de servicios": servicios agrupados por fecha, con filtros. */
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
        else -> CalendarioServicios(
            estado = estado,
            onFiltro = viewModel::cambiarFiltro,
            onVerDetalle = onVerDetalle
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarioServicios(
    estado: ServiciosUiState,
    onFiltro: (FiltroServicios) -> Unit,
    onVerDetalle: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        // Filtros (FilterChip de Material 3); el seleccionado usa el turquesa suave
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroServicios.entries.forEach { filtro ->
                FilterChip(
                    selected = estado.filtro == filtro,
                    onClick = { onFiltro(filtro) },
                    label = { Text(filtro.etiqueta) }
                )
            }
        }

        Text(
            "Mostrando ${estado.cantidad} de ${estado.total} servicios",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (estado.porFecha.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay servicios en esta categoría.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Recorremos el MAP fecha -> servicios: un título por día y sus tarjetas debajo
                estado.porFecha.forEach { (fecha, serviciosDelDia) ->
                    item(key = "fecha-$fecha") {
                        Text(
                            Formatos.fechaLegible(fecha),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(serviciosDelDia, key = { it.id }) { servicio ->
                        TarjetaServicio(servicio = servicio, onClick = { onVerDetalle(servicio.id) })
                    }
                }
            }
        }
    }
}
