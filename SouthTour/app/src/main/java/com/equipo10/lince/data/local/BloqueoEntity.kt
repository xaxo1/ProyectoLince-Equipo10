package com.equipo10.lince.data.local

import androidx.room.Entity

/**
 * ENTITY de Room: un día bloqueado en "Disponibilidad".
 * Clave primaria compuesta: un usuario no puede bloquear dos veces la misma fecha.
 */
@Entity(tableName = "bloqueos", primaryKeys = ["usuarioId", "fecha"])
data class BloqueoEntity(
    val usuarioId: Int,
    val fecha: String                   // "yyyy-MM-dd"
)
