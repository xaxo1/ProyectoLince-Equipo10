package com.equipo10.lince.model

/** Roles de la app: el pasajero no inicia sesión. */
enum class Rol(val etiqueta: String) {
    GUIA("Guía"),
    TRANSPORTISTA("Transportista"),
    COORDINADOR("Coordinación")
}

/**
 * Usuario con sesión iniciada (lo entrega el backend en el login).
 * [recursoId] = id del guía o transportista en la Lince API; null para coordinación.
 */
data class Usuario(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rut: String,
    val rol: Rol,
    val recursoId: Int?
) {
    /** "Camila Soto" -> "CS" */
    val iniciales: String
        get() = nombre.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")

    companion object {
        /** Quita puntos, guion y espacios: "15.482.910-k" -> "15482910K" */
        fun normalizarRut(rut: String): String = rut.filter { it.isLetterOrDigit() }.uppercase()
    }
}
