package com.equipo10.lince.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.R
import com.equipo10.lince.model.EstadosLince
import com.equipo10.lince.model.TipoServicio
import com.equipo10.lince.ui.components.BannerMensaje
import com.equipo10.lince.ui.components.Etiqueta
import com.equipo10.lince.viewmodel.PestanaSouthbound
import com.equipo10.lince.viewmodel.SouthboundViewModel

/**
 * Catálogo de Southbound: consume la Lince API EXTERNA (solo lectura) con paginación.
 * Servicios (con filtro por tipo), guías, transportistas y catálogos de códigos.
 */
@Composable
fun SouthboundScreen(viewModel: SouthboundViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { if (estado.pagina == 0) viewModel.cargarPagina(1) }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = estado.pestana.ordinal) {
            PestanaSouthbound.entries.forEach { p ->
                Tab(selected = estado.pestana == p, onClick = { viewModel.cambiarPestana(p) }, text = { Text(p.etiqueta, maxLines = 1) })
            }
        }
        if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())

        if (estado.pestana == PestanaSouthbound.SERVICIOS) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipoServicio.entries.forEach { t ->
                    FilterChip(selected = estado.tipo == t, onClick = { viewModel.cambiarTipo(t) }, label = { Text(t.name) })
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            estado.error?.let { item { BannerMensaje(texto = it, esAlerta = true) } }
            if (estado.pestana != PestanaSouthbound.CATALOGOS) {
                item {
                    Text(stringResource(R.string.pagina_de, estado.pagina, estado.totalPaginas, estado.total),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            when (estado.pestana) {
                PestanaSouthbound.SERVICIOS -> items(estado.servicios, key = { it.id }) { s ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row {
                                Text("${s.codigo} · ${s.fecha} ${s.horaPickup}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Etiqueta(s.estadoLince, if (s.estadoLince == "XX") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
                            }
                            Text(s.excursion)
                            Text("${s.tipo.name} · ${s.pasajeros} pax · PL ${s.statusPL ?: "—"} (${EstadosLince.asignacion(s.statusPL)})",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                PestanaSouthbound.GUIAS, PestanaSouthbound.TRANSPORTISTAS -> items(estado.recursos, key = { it.id }) { r ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${r.codigo} · ${r.nombre}", fontWeight = FontWeight.SemiBold)
                            Text("${r.oficina} · ${r.detalle}", style = MaterialTheme.typography.bodySmall)
                            r.evaluacion?.let { Text("★ $it", style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
                PestanaSouthbound.CATALOGOS -> estado.catalogos?.let { c ->
                    item { Seccion(stringResource(R.string.oficina)) { c.oficinas.forEach { Text(it) } } }
                    item { Seccion(stringResource(R.string.tipos_servicio)) { c.tiposServicio.forEach { Text(it) } } }
                    item { Seccion(stringResource(R.string.estado_servicio)) { c.estadosServicio.forEach { (k, v) -> FilaDetalle(k, v) } } }
                    item { Seccion("StatusPL") { c.statusPL.forEach { (k, v) -> FilaDetalle(k, v) } } }
                }
            }
            if (estado.hayMas && estado.pestana != PestanaSouthbound.CATALOGOS) {
                item {
                    OutlinedButton(onClick = viewModel::cargarMas, enabled = !estado.cargando, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.cargar_mas))
                    }
                }
            }
        }
    }
}
