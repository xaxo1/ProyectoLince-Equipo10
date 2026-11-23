package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.ServiceLocator
import com.equipo10.lince.model.PerfilUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repositorio: RepositorioLince = ServiceLocator.repositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            repositorio.perfil()
                .onSuccess { perfil -> _uiState.update { it.copy(cargando = false, perfil = perfil) } }
                .onFailure { e -> _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudo cargar el perfil") } }
        }
    }

    /** Borra el JWT guardado en DataStore y avisa a la pantalla cuando terminó. */
    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            repositorio.cerrarSesion()
            alTerminar()
        }
    }
}
