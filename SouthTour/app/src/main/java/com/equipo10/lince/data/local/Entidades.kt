package com.equipo10.lince.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

/**
 * ENTITY: hito (check-in / check-out) registrado SIN conexión, esperando sincronizarse.
 * [idempotencia] es la misma clave que se envía al backend: si el envío se repite, no se duplica.
 */
@Entity(tableName = "hitos_pendientes")
data class HitoPendienteEntity(
    @PrimaryKey val idempotencia: String,
    val servicioId: Int,
    val tipo: String,
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Double,
    val fechaHoraDispositivo: String,
    val intentos: Int = 0,
    val ultimoError: String? = null
)

/** ENTITY: caché de los servicios leídos de la Lince API (para verlos sin internet). */
@Entity(tableName = "servicios_cache")
data class ServicioCacheEntity(
    @PrimaryKey val id: Int,
    val fecha: String,
    val json: String          // el ServicioLinceDto completo en JSON
)

/** ENTITY: foto tomada para una incidencia (la foto queda en el teléfono; el texto va al backend). */
@Entity(tableName = "fotos_incidencia")
data class FotoIncidenciaEntity(
    @PrimaryKey val incidenciaId: Long,
    val rutaFoto: String
)

/** DAO de la cola de hitos pendientes. */
@Dao
interface HitoPendienteDao {
    @Query("SELECT * FROM hitos_pendientes ORDER BY fechaHoraDispositivo")
    suspend fun todos(): List<HitoPendienteEntity>

    @Query("SELECT * FROM hitos_pendientes WHERE servicioId = :servicioId")
    suspend fun delServicio(servicioId: Int): List<HitoPendienteEntity>

    @Query("SELECT COUNT(*) FROM hitos_pendientes")
    suspend fun cantidad(): Int

    @Upsert
    suspend fun guardar(hito: HitoPendienteEntity)

    @Query("DELETE FROM hitos_pendientes WHERE idempotencia = :idempotencia")
    suspend fun eliminar(idempotencia: String)
}

/** DAO de la caché de servicios. */
@Dao
interface ServicioCacheDao {
    @Upsert
    suspend fun guardar(servicios: List<ServicioCacheEntity>)

    @Query("SELECT * FROM servicios_cache WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha")
    suspend fun entre(desde: String, hasta: String): List<ServicioCacheEntity>

    @Query("SELECT * FROM servicios_cache WHERE id = :id")
    suspend fun porId(id: Int): ServicioCacheEntity?
}

/** DAO de las fotos de incidencias. */
@Dao
interface FotoIncidenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(foto: FotoIncidenciaEntity)

    @Query("SELECT * FROM fotos_incidencia")
    suspend fun todas(): List<FotoIncidenciaEntity>
}
