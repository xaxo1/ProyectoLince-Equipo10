package com.equipo10.lince.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.equipo10.lince.util.Formatos

/**
 * DATABASE de Room: el archivo SQLite "southtour.db" con sus 3 tablas.
 * Se crea UNA sola instancia para toda la app (patrón Singleton).
 */
@Database(
    entities = [RegistroServicioEntity::class, BloqueoEntity::class, IncidenciaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LinceDatabase : RoomDatabase() {

    abstract fun registroServicioDao(): RegistroServicioDao
    abstract fun bloqueoDao(): BloqueoDao
    abstract fun incidenciaDao(): IncidenciaDao

    companion object {
        @Volatile
        private var instancia: LinceDatabase? = null

        fun obtener(context: Context): LinceDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    LinceDatabase::class.java,
                    "southtour.db"
                )
                    .addCallback(DatosIniciales)
                    .build()
                    .also { instancia = it }
            }
    }

    /** Se ejecuta SOLO la primera vez que se crea la base: deja un día bloqueado de ejemplo para la guía. */
    private object DatosIniciales : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL(
                "INSERT INTO bloqueos (usuarioId, fecha) VALUES (?, ?)",
                arrayOf<Any>(1, Formatos.fechaEnDias(14))
            )
        }
    }
}
