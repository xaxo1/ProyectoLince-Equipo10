package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.model.LoginUiState
import com.equipo10.lince.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    // ENCAPSULAMIENTO (Clase 6): el MutableStateFlow es privado; la pantalla solo LEE el StateFlow
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentificadorChange(valor: String) {
        _uiState.update { actual -> actual.copy(identificador = valor, errorIdentificador = null, error = null) }
    }

    fun onClaveChange(valor: String) {
        _uiState.update { actual -> actual.copy(clave = valor, errorClave = null, error = null) }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando) return

        // 1) Validación de los campos: cada error se muestra bajo su campo
        val errorIdentificador = validarIdentificador(estado.identificador)
        val errorClave = if (estado.clave.isBlank()) "Ingresa tu contraseña" else null
        if (errorIdentificador != null || errorClave != null) {
            _uiState.update { it.copy(errorIdentificador = errorIdentificador, errorClave = errorClave) }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }

        // CORRUTINA: viewModelScope la cancela sola si el ViewModel se destruye
        viewModelScope.launch {
            RepositorioLince.login(estado.identificador, estado.clave)
                .onSuccess {
                    _uiState.update { it.copy(cargando = false, loginExitoso = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(cargando = false, error = e.message ?: "Error desconocido") }
                }
        }
    }

    /** Reglas del campo RUT / correo. Devuelve el mensaje de error o null si es válido. */
    private fun validarIdentificador(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "Ingresa tu RUT o tu correo"
            "@" in texto ->
                if (REGEX_CORREO.matches(texto)) null else "El correo no tiene un formato válido"
            !REGEX_RUT.matches(Usuario.normalizarRut(texto)) -> "El RUT debe tener el formato 12.345.678-9"
            else -> null
        }
    }

    /** Cierra el aviso de error ("Reintentar" del diseño). */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    /** Se llama después de navegar para no repetir la navegación. */
    fun navegacionRealizada() {
        _uiState.update { it.copy(loginExitoso = false) }
    }

    private companion object {
        val REGEX_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")
        val REGEX_RUT = Regex("^\\d{7,8}[0-9K]$")       // 7 u 8 dígitos + dígito verificador
    }
}
