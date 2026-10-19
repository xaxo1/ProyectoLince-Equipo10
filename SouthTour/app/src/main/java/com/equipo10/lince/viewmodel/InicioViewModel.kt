package com.equipo10.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.model.InicioUiState
import com.equipo10.lince.util.Formatos
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel de "Servicios del día" (Inicio). */
class InicioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }

            val usuario = RepositorioLince.usuarioActual
            if (usuario == null) {
                _uiState.update { it.copy(cargando = false, error = "No hay una sesión iniciada") }
                return@launch
            }

            // ASYNC: las dos consultas corren EN PARALELO; await() espera cada resultado
            val hoyDiferido = async { RepositorioLince.obtenerServiciosDelDia() }
            val proximoDiferido = async { RepositorioLince.obtenerProximoServicio() }

            val serviciosHoy = hoyDiferido.await().getOrElse { e ->
                _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudieron cargar los servicios") }
                return@launch
            }
            val proximo = proximoDiferido.await().getOrNull()

            _uiState.update {
                it.copy(
                    cargando = false,
                    nombre = usuario.nombre,
                    rol = usuario.rol.etiqueta,
                    fechaHoy = Formatos.fechaLegible(Formatos.hoy()),
                    serviciosHoy = serviciosHoy,
                    proximo = proximo
                )
            }
        }
    }
}
