package com.equipo10.lince.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.model.IncidenciaUiState
import com.equipo10.lince.model.TipoIncidencia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del formulario "Reportar incidencia".
 * Las reglas de validación viven aquí (no en la pantalla): la UI solo muestra los errores del UiState.
 */
class IncidenciaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(IncidenciaUiState())
    val uiState: StateFlow<IncidenciaUiState> = _uiState.asStateFlow()

    fun cargar(idServicio: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            RepositorioLince.obtenerServicio(idServicio)
                .onSuccess { servicio -> _uiState.update { it.copy(cargando = false, servicio = servicio) } }
                .onFailure { e -> _uiState.update { it.copy(cargando = false, error = e.message ?: "No se pudo cargar el servicio") } }
        }
    }

    // ----- Cambios en los campos: al escribir se borra el error de ese campo -----

    fun onTipoChange(tipo: TipoIncidencia) {
        _uiState.update { it.copy(tipo = tipo, errorTipo = null, errorPasajeros = null) }
    }

    fun onDescripcionChange(valor: String) {
        if (valor.length > MAX_DESCRIPCION) return     // no deja escribir más del máximo
        _uiState.update { it.copy(descripcion = valor, errorDescripcion = null) }
    }

    fun onPasajerosChange(valor: String) {
        _uiState.update { it.copy(pasajerosPresentes = valor, errorPasajeros = null) }
    }

    /** Resultado de la cámara (TakePicturePreview): null si el usuario canceló. */
    fun onFotoTomada(foto: Bitmap?) {
        if (foto != null) _uiState.update { it.copy(foto = foto, aviso = null) }
    }

    fun quitarFoto() {
        _uiState.update { it.copy(foto = null) }
    }

    fun mostrarAviso(texto: String) {
        _uiState.update { it.copy(aviso = texto) }
    }

    fun guardar() {
        val estado = _uiState.value
        val servicio = estado.servicio ?: return
        if (estado.guardando) return

        // 1) Validar cada campo
        val errorTipo = if (estado.tipo == null) "Selecciona el tipo de incidencia" else null
        val errorDescripcion = validarDescripcion(estado.descripcion)
        val errorPasajeros = validarPasajeros(estado.pasajerosPresentes, servicio.pasajeros, estado.tipo)

        if (errorTipo != null || errorDescripcion != null || errorPasajeros != null) {
            _uiState.update {
                it.copy(
                    errorTipo = errorTipo,
                    errorDescripcion = errorDescripcion,
                    errorPasajeros = errorPasajeros,
                    aviso = "Revisa los campos marcados en rojo"
                )
            }
            return
        }

        // 2) Todo válido: guardar en Room
        val tipo = estado.tipo ?: return
        _uiState.update { it.copy(guardando = true, aviso = null) }
        viewModelScope.launch {
            RepositorioLince.registrarIncidencia(
                servicioId = servicio.id,
                tipo = tipo,
                descripcion = estado.descripcion,
                pasajerosPresentes = estado.pasajerosPresentes.trim().toInt(),
                foto = estado.foto
            )
                .onSuccess { incidencia -> _uiState.update { it.copy(guardando = false, guardada = incidencia) } }
                .onFailure { e -> _uiState.update { it.copy(guardando = false, aviso = e.message ?: "No se pudo guardar") } }
        }
    }

    // ----- Reglas de validación -----

    private fun validarDescripcion(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "La descripción es obligatoria"
            texto.length < MIN_DESCRIPCION -> "Describe lo ocurrido con al menos $MIN_DESCRIPCION caracteres"
            else -> null
        }
    }

    private fun validarPasajeros(valor: String, reservados: Int, tipo: TipoIncidencia?): String? {
        val texto = valor.trim()
        if (texto.isEmpty()) return "Indica cuántos pasajeros están presentes"
        val cantidad = texto.toIntOrNull() ?: return "Ingresa solo números"
        return when {
            cantidad !in 0..reservados -> "Debe ser un número entre 0 y $reservados (pasajeros de la reserva)"
            tipo == TipoIncidencia.PASAJERO_AUSENTE && cantidad == reservados ->
                "Si falta un pasajero, los presentes deben ser menos de $reservados"
            else -> null
        }
    }

    companion object {
        const val MIN_DESCRIPCION = 10
        const val MAX_DESCRIPCION = 300
    }
}
