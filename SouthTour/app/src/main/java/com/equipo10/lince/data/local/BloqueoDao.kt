package com.equipo10.lince.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** DAO: consultas de la tabla "bloqueos". */
@Dao
interface BloqueoDao {

    @Query("SELECT fecha FROM bloqueos WHERE usuarioId = :usuarioId")
    suspend fun fechasDelUsuario(usuarioId: Int): List<String>

    /** IGNORE: si la fecha ya estaba bloqueada no se inserta y devuelve -1. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(bloqueo: BloqueoEntity): Long

    @Query("DELETE FROM bloqueos WHERE usuarioId = :usuarioId AND fecha = :fecha")
    suspend fun eliminar(usuarioId: Int, fecha: String)
}
