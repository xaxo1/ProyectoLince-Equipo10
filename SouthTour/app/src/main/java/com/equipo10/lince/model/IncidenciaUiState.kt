package com.equipo10.lince.model

import android.graphics.Bitmap

/**
 * Estado del formulario "Reportar incidencia".
 * Cada campo tiene su propio mensaje de error (null = sin error).
 */
data class IncidenciaUiState(
    val cargando: Boolean = true,
    val servicio: Servicio? = null,
    val tipo: TipoIncidencia? = null,
    val descripcion: String = "",
    val pasajerosPresentes: String = "",    // String: es lo que escribe el usuario en el campo
    val foto: Bitmap? = null,               // foto tomada con la cámara (opcional)
    val errorTipo: String? = null,
    val errorDescripcion: String? = null,
    val errorPasajeros: String? = null,
    val aviso: String? = null,              // mensaje general (ej: regla de negocio o cámara no disponible)
    val guardando: Boolean = false,
    val guardada: Incidencia? = null,       // != null -> se muestra la confirmación
    val error: String? = null               // error al cargar el servicio
)
