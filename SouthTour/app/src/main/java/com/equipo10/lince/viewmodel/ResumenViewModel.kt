package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.model.ResumenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel del "Resumen" mensual: total acumulado, completados, cancelados pagados y puntualidad. */
class ResumenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResumenUiState())
    val uiState: StateFlow<ResumenUiState> = _uiState.asStateFlow()

    fun cargar() = cargarMes(_uiState.value.desplazamientoMes)

    fun mesAnterior() = cargarMes(_uiState.value.desplazamientoMes - 1)

    /** No se puede avanzar más allá del mes actual. */
    fun mesSiguiente() {
        val actual = _uiState.value.desplazamientoMes
        if (actual < 0) cargarMes(actual + 1)
    }

    fun volverAlMesActual() = cargarMes(0)

    private fun cargarMes(desplazamiento: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null, desplazamientoMes = desplazamiento) }
            RepositorioLince.obtenerResumen(desplazamiento)
                .onSuccess { resumen -> _uiState.update { it.copy(cargando = false, resumen = resumen) } }
                .onFailure { e -> _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudo cargar el resumen") } }
        }
    }
}
