package com.equipo10.lince.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * DATABASE de Room ("southtour.db"). Desde la versión 2 los datos oficiales viven en el backend;
 * aquí queda lo necesario para trabajar SIN conexión: caché de servicios, cola de hitos pendientes
 * y las fotos de las incidencias.
 */
@Database(
    entities = [HitoPendienteEntity::class, ServicioCacheEntity::class, FotoIncidenciaEntity::class],
    version = 2,
    exportSchema = false
)
abstract class LinceDatabase : RoomDatabase() {

    abstract fun hitoPendienteDao(): HitoPendienteDao
    abstract fun servicioCacheDao(): ServicioCacheDao
    abstract fun fotoIncidenciaDao(): FotoIncidenciaDao

    companion object {
        @Volatile
        private var instancia: LinceDatabase? = null

        fun obtener(context: Context): LinceDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(context.applicationContext, LinceDatabase::class.java, "southtour.db")
                    // La versión 1 tenía otras tablas: se recrea la base (solo había datos de prueba)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}
