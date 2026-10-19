package com.equipo10.lince.model

/** Asignación hecha por operaciones: relaciona un servicio con un guía o transportista. */
data class Asignacion(
    val id: Int,
    val servicio: Servicio,
    val usuarioId: Int
)
