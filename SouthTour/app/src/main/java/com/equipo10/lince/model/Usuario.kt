package com.equipo10.lince.model

/** Contrato común: todo lo que se puede "informar" en pantalla (POLIMORFISMO). */
interface Informable {
    fun informar(): String
}

/** Roles de la app: solo guías y transportistas (el pasajero no usa la app). */
enum class Rol(val etiqueta: String) {
    GUIA("Guía"),
    TRANSPORTISTA("Transportista")
}

/**
 * Clase base "open" para permitir HERENCIA.
 * La clave es private: nadie fuera de Usuario puede leerla (ENCAPSULAMIENTO).
 */
open class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    private val clave: String,
    val rol: Rol
) : Informable {

    fun validarClave(intento: String): Boolean = clave == intento

    /** "Camila Soto" -> "CS" (colecciones: split + mapNotNull + take) */
    val iniciales: String
        get() = nombre.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")

    override fun informar(): String = "$nombre · ${rol.etiqueta}"
}

/** HERENCIA: un Guía es un Usuario que además maneja idiomas. */
class Guia(
    id: Int,
    nombre: String,
    email: String,
    clave: String,
    val idiomas: List<String>
) : Usuario(id, nombre, email, clave, Rol.GUIA) {

    // POLIMORFISMO: sobrescribe el comportamiento de la clase padre
    override fun informar(): String =
        "${super.informar()} · Idiomas: ${idiomas.joinToString(", ")}"
}

/** HERENCIA: un Transportista es un Usuario con licencia y (quizás) vehículo. */
class Transportista(
    id: Int,
    nombre: String,
    email: String,
    clave: String,
    val licencia: String,
    val vehiculo: String?          // NULL SAFETY: puede no tener vehículo asignado
) : Usuario(id, nombre, email, clave, Rol.TRANSPORTISTA) {

    override fun informar(): String =
        "${super.informar()} · Licencia $licencia · ${vehiculo ?: "Sin vehículo asignado"}"
}
