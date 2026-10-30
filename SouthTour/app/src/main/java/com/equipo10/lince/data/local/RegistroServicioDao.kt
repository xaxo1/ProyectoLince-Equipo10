package com.equipo10.lince.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/** DAO: las consultas SQL de la tabla "registros_servicio". Room genera la implementación. */
@Dao
interface RegistroServicioDao {

    @Query("SELECT * FROM registros_servicio WHERE usuarioId = :usuarioId")
    suspend fun obtenerDelUsuario(usuarioId: Int): List<RegistroServicioEntity>

    @Query("SELECT * FROM registros_servicio WHERE servicioId = :servicioId")
    suspend fun obtenerPorServicio(servicioId: Int): RegistroServicioEntity?

    /** UPSERT: inserta el registro o lo actualiza si el servicio ya tenía uno (check-in -> check-out). */
    @Upsert
    suspend fun guardar(registro: RegistroServicioEntity)

    @Query("DELETE FROM registros_servicio WHERE servicioId = :servicioId")
    suspend fun eliminar(servicioId: Int)
}
