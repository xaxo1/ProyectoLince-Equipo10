package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    // ENCAPSULAMIENTO (Clase 6): el MutableStateFlow es privado; la pantalla solo LEE el StateFlow
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(valor: String) {
        _uiState.update { actual -> actual.copy(email = valor, error = null) }
    }

    fun onClaveChange(valor: String) {
        _uiState.update { actual -> actual.copy(clave = valor, error = null) }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando) return

        _uiState.update { it.copy(cargando = true, error = null) }

        // CORRUTINA: viewModelScope la cancela sola si el ViewModel se destruye
        viewModelScope.launch {
            RepositorioLince.login(estado.email, estado.clave)
                .onSuccess {
                    _uiState.update { it.copy(cargando = false, loginExitoso = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(cargando = false, error = e.message ?: "Error desconocido") }
                }
        }
    }

    /** Se llama después de navegar para no repetir la navegación. */
    fun navegacionRealizada() {
        _uiState.update { it.copy(loginExitoso = false) }
    }
}
