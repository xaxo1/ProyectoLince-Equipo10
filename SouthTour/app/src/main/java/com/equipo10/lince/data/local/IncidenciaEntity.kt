package com.equipo10.lince.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** ENTITY de Room: una incidencia reportada durante un servicio (formulario + foto). */
@Entity(tableName = "incidencias")
data class IncidenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,   // Room asigna el id al insertar
    val servicioId: Int,
    val usuarioId: Int,
    val tipo: String,                   // nombre del enum TipoIncidencia
    val descripcion: String,
    val pasajerosPresentes: Int,
    val rutaFoto: String?,              // archivo .jpg en la memoria interna de la app (opcional)
    val registradaEn: String            // "yyyy-MM-dd HH:mm"
)
