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
    val rut: String,               // identificador principal en terreno (como en el diseño de Stitch)
    val email: String,
    private val clave: String,
    val rol: Rol
) : Informable {

    fun validarClave(intento: String): Boolean = clave == intento

    /**
     * Permite ingresar con RUT (con o sin puntos y guion) o con el correo.
     * "15.482.910-K", "15482910k" y "guia@southbound.cl" identifican al mismo usuario.
     */
    fun coincideCon(identificador: String): Boolean {
        val limpio = identificador.trim()
        return limpio.equals(email, ignoreCase = true) || normalizarRut(limpio) == normalizarRut(rut)
    }

    /** "Camila Soto" -> "CS" (colecciones: split + mapNotNull + take) */
    val iniciales: String
        get() = nombre.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")

    override fun informar(): String = "$nombre · ${rol.etiqueta}"

    companion object {
        /** Quita puntos, guion y espacios: "15.482.910-k" -> "15482910K" */
        fun normalizarRut(rut: String): String =
            rut.filter { it.isLetterOrDigit() }.uppercase()
    }
}

/** HERENCIA: un Guía es un Usuario que además maneja idiomas. */
class Guia(
    id: Int,
    nombre: String,
    rut: String,
    email: String,
    clave: String,
    val idiomas: List<String>
) : Usuario(id, nombre, rut, email, clave, Rol.GUIA) {

    // POLIMORFISMO: sobrescribe el comportamiento de la clase padre
    override fun informar(): String =
        "${super.informar()} · Idiomas: ${idiomas.joinToString(", ")}"
}

/** HERENCIA: un Transportista es un Usuario con licencia y (quizás) vehículo. */
class Transportista(
    id: Int,
    nombre: String,
    rut: String,
    email: String,
    clave: String,
    val licencia: String,
    val vehiculo: String?          // NULL SAFETY: puede no tener vehículo asignado
) : Usuario(id, nombre, rut, email, clave, Rol.TRANSPORTISTA) {

    override fun informar(): String =
        "${super.informar()} · Licencia $licencia · ${vehiculo ?: "Sin vehículo asignado"}"
}
