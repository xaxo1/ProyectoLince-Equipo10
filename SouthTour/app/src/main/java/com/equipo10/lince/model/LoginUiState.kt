package com.equipo10.lince.model

/** Estado completo de la pantalla de Login. */
data class LoginUiState(
    val identificador: String = "",     // RUT o correo
    val clave: String = "",
    val errorIdentificador: String? = null,  // error de validación bajo el campo RUT / correo
    val errorClave: String? = null,          // error de validación bajo el campo contraseña
    val cargando: Boolean = false,
    val error: String? = null,
    val loginExitoso: Boolean = false
)
