package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.ServiceLocator
import com.equipo10.lince.data.remoto.ApiExcepcion
import com.equipo10.lince.model.Catalogos
import com.equipo10.lince.model.RecursoSouthbound
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.model.TipoServicio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pestañas del catálogo Southbound (Lince API). */
enum class PestanaSouthbound(val etiqueta: String) { SERVICIOS("Servicios"), GUIAS("Guías"), TRANSPORTISTAS("Transportistas"), CATALOGOS("Catálogos") }

data class SouthboundUiState(
    val pestana: PestanaSouthbound = PestanaSouthbound.SERVICIOS,
    val cargando: Boolean = false,
    val servicios: List<Servicio> = emptyList(),
    val tipo: TipoServicio? = null,
    val recursos: List<RecursoSouthbound> = emptyList(),
    val catalogos: Catalogos? = null,
    val pagina: Int = 0,
    val totalPaginas: Int = 0,
    val total: Int = 0,
    val error: String? = null          // incluye el requestId cuando la API lo entrega
) {
    val hayMas: Boolean get() = pagina < totalPaginas
}

/**
 * ViewModel del catálogo Southbound: consume la API EXTERNA del cliente (solo lectura)
 * con paginación (page / pageSize, data + meta) y muestra los errores con su requestId.
 */
class SouthboundViewModel(
    private val repositorio: RepositorioLince = ServiceLocator.repositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(SouthboundUiState())
    val uiState: StateFlow<SouthboundUiState> = _uiState.asStateFlow()

    fun cambiarPestana(pestana: PestanaSouthbound) {
        _uiState.update { SouthboundUiState(pestana = pestana, tipo = it.tipo) }
        cargarPagina(1)
    }

    fun cambiarTipo(tipo: TipoServicio?) {
        _uiState.update { it.copy(tipo = if (it.tipo == tipo) null else tipo, servicios = emptyList()) }
        cargarPagina(1)
    }

    /** Página siguiente (botón "Cargar más"). */
    fun cargarMas() {
        val estado = _uiState.value
        if (estado.hayMas && !estado.cargando) cargarPagina(estado.pagina + 1)
    }

    fun cargarPagina(pagina: Int) {
        val estado = _uiState.value
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            when (estado.pestana) {
                PestanaSouthbound.SERVICIOS -> repositorio.serviciosSouthbound(pagina, estado.tipo)
                    .onSuccess { p ->
                        _uiState.update {
                            it.copy(cargando = false, servicios = (if (pagina == 1) emptyList() else it.servicios) + p.items,
                                pagina = p.pagina, totalPaginas = p.totalPaginas, total = p.total)
                        }
                    }.onFailure(::mostrarError)
                PestanaSouthbound.GUIAS, PestanaSouthbound.TRANSPORTISTAS ->
                    repositorio.recursosSouthbound(estado.pestana == PestanaSouthbound.GUIAS, pagina)
                        .onSuccess { p ->
                            _uiState.update {
                                it.copy(cargando = false, recursos = (if (pagina == 1) emptyList() else it.recursos) + p.items,
                                    pagina = p.pagina, totalPaginas = p.totalPaginas, total = p.total)
                            }
                        }.onFailure(::mostrarError)
                PestanaSouthbound.CATALOGOS -> repositorio.catalogos()
                    .onSuccess { c -> _uiState.update { it.copy(cargando = false, catalogos = c) } }
                    .onFailure(::mostrarError)
            }
        }
    }

    private fun mostrarError(e: Throwable) {
        val requestId = (e as? ApiExcepcion)?.requestId
        val texto = (e.message ?: "Error al consultar la Lince API") + (requestId?.let { " (requestId: $it)" } ?: "")
        _uiState.update { it.copy(cargando = false, error = texto) }
    }
}
