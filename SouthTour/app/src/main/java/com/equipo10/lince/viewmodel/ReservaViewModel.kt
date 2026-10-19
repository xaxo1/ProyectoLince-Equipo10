package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.model.ReservaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel de la "Reserva completa". */
class ReservaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReservaUiState())
    val uiState: StateFlow<ReservaUiState> = _uiState.asStateFlow()

    fun cargar(numeroReserva: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            RepositorioLince.obtenerReserva(numeroReserva)
                .onSuccess { reserva -> _uiState.update { it.copy(cargando = false, reserva = reserva) } }
                .onFailure { e -> _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudo cargar la reserva") } }
        }
    }
}
