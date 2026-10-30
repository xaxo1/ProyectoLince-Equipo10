package com.equipo10.lince.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ENTITY de Room: una fila de la tabla "registros_servicio".
 * Guarda en el teléfono el check-in / check-out de un servicio (trazabilidad),
 * así el registro se conserva aunque se cierre la app.
 */
@Entity(tableName = "registros_servicio")
data class RegistroServicioEntity(
    @PrimaryKey val servicioId: Int,
    val usuarioId: Int,
    val estado: String,                 // nombre del enum EstadoServicio ("EN_CURSO", "FINALIZADO")
    val horaCheckIn: String?,
    val horaCheckOut: String?,
    val conAtraso: Boolean,
    val ubicacionCheckIn: String?,      // "-33.43722, -70.65041" (GPS)
    val ubicacionCheckOut: String?
)
