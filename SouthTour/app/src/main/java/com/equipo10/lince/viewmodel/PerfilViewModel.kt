package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.model.PerfilUiState
import com.equipo10.lince.model.SesionNoIniciadaException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            // Control de errores TRADICIONAL: try - catch - finally
            try {
                val perfil = RepositorioLince.obtenerPerfil()
                _uiState.update { it.copy(perfil = perfil) }
            } catch (e: SesionNoIniciadaException) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                // finally se ejecuta SIEMPRE, haya error o no
                _uiState.update { it.copy(cargando = false) }
            }
        }
    }

    fun cerrarSesion() {
        RepositorioLince.cerrarSesion()
    }
}
