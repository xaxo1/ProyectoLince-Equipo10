package com.equipo10.lince.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** DAO: consultas de la tabla "incidencias". */
@Dao
interface IncidenciaDao {

    /**
     * Devuelve un FLOW: cada vez que cambia la tabla, Room emite la lista nueva
     * y la pantalla de Detalle se actualiza sola.
     */
    @Query("SELECT * FROM incidencias WHERE servicioId = :servicioId ORDER BY id DESC")
    fun observarDelServicio(servicioId: Int): Flow<List<IncidenciaEntity>>

    /** Devuelve el id generado por Room. */
    @Insert
    suspend fun insertar(incidencia: IncidenciaEntity): Long
}
