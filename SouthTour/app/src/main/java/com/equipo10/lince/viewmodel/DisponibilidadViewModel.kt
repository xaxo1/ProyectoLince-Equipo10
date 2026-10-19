package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.model.DisponibilidadUiState
import com.equipo10.lince.model.EstadoDia
import com.equipo10.lince.util.Formatos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel de "Disponibilidad": bloquear y desbloquear días. */
class DisponibilidadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DisponibilidadUiState())
    val uiState: StateFlow<DisponibilidadUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            refrescarDias()
        }
    }

    fun bloquear(fecha: String) {
        viewModelScope.launch {
            RepositorioLince.bloquearDia(fecha)
                .onSuccess {
                    mostrarMensaje("Día bloqueado: ${Formatos.fechaLegible(fecha)}", esAlerta = false)
                    refrescarDias()
                }
                .onFailure { e -> mostrarMensaje(e.message ?: "No se pudo bloquear", esAlerta = true) }
        }
    }

    fun desbloquear(fecha: String) {
        viewModelScope.launch {
            RepositorioLince.desbloquearDia(fecha)
                .onSuccess {
                    mostrarMensaje("Día disponible nuevamente: ${Formatos.fechaLegible(fecha)}", esAlerta = false)
                    refrescarDias()
                }
                .onFailure { e -> mostrarMensaje(e.message ?: "No se pudo desbloquear", esAlerta = true) }
        }
    }

    private fun mostrarMensaje(texto: String, esAlerta: Boolean) {
        _uiState.update { it.copy(mensaje = texto, mensajeEsAlerta = esAlerta) }
    }

    /** suspend: solo se puede llamar desde una corrutina. */
    private suspend fun refrescarDias() {
        RepositorioLince.obtenerDisponibilidad()
            .onSuccess { dias ->
                _uiState.update {
                    it.copy(
                        cargando = false,
                        dias = dias,
                        bloqueados = dias.count { dia -> dia.estado == EstadoDia.BLOQUEADO }
                    )
                }
            }
            .onFailure { e ->
                _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudo cargar la disponibilidad") }
            }
    }
}
