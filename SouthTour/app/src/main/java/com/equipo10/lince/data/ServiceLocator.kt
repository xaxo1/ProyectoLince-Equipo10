package com.equipo10.lince.data

import android.content.Context
import android.graphics.Bitmap
import com.equipo10.lince.data.local.LinceDatabase
import com.equipo10.lince.data.remoto.Red
import com.equipo10.lince.sync.SincronizarHitosWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Arma las dependencias UNA vez (lo llama LinceApplication) y las comparte con los ViewModels.
 * En las pruebas los ViewModels reciben un repositorio falso (MockK) por el constructor.
 */
object ServiceLocator {

    lateinit var sesion: SesionDataStore
        private set
    lateinit var repositorio: RepositorioLince
        private set

    fun inicializar(context: Context) {
        val app = context.applicationContext
        sesion = SesionDataStore(app)
        runBlocking { sesion.cargar() }   // recupera la sesión guardada antes de mostrar la primera pantalla

        val db = LinceDatabase.obtener(app)
        val carpetaFotos = File(app.filesDir, "incidencias").apply { mkdirs() }

        repositorio = RepositorioLince(
            backend = Red.backendApi { sesion.token },
            lince = Red.linceApi(),
            sesion = sesion,
            pendientes = db.hitoPendienteDao(),
            cache = db.servicioCacheDao(),
            fotos = db.fotoIncidenciaDao(),
            almacenFotos = { foto -> guardarFoto(carpetaFotos, foto) },
            programarSincronizacion = { SincronizarHitosWorker.programar(app) }
        )
    }

    /** Escribe la foto como .jpg en la memoria interna de la app (hilo IO: no bloquea la pantalla). */
    private suspend fun guardarFoto(carpeta: File, foto: Bitmap): String = withContext(Dispatchers.IO) {
        val archivo = File(carpeta, "incidencia_${System.currentTimeMillis()}.jpg")
        archivo.outputStream().use { foto.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        archivo.absolutePath
    }
}
