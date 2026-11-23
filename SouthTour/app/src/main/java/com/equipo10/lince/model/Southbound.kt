package com.equipo10.lince.model

/** Lista de servicios + si vino de la caché de Room (sin internet). */
data class ListaServicios(val servicios: List<Servicio>, val desdeCache: Boolean = false)

/** Una página de la Lince API (paginación data + meta). */
data class Pagina<T>(val items: List<T>, val pagina: Int, val totalPaginas: Int, val total: Int) {
    val hayMas: Boolean get() = pagina < totalPaginas
}

/** Guía o transportista de la Lince API (catálogo de recursos). */
data class RecursoSouthbound(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val oficina: String,
    val detalle: String,            // idiomas del guía o vehículos del transportista
    val evaluacion: Double?
)

/** Catálogos de la Lince API: códigos y su descripción. */
data class Catalogos(
    val oficinas: List<String>,
    val tiposServicio: List<String>,
    val estadosServicio: List<Pair<String, String>>,
    val statusPL: List<Pair<String, String>>
)
