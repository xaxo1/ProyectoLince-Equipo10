package com.equipo10.lince.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.equipo10.lince.model.Rol
import com.equipo10.lince.model.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.almacen by preferencesDataStore(name = "sesion")

/** Lo que el repositorio necesita de la sesión (interfaz: en las pruebas se reemplaza por un mock). */
interface Sesion {
    val token: String?
    val usuario: Usuario?
    suspend fun guardar(token: String, usuario: Usuario)
    suspend fun cerrar()
}

/**
 * DATASTORE: guarda el JWT y los datos del usuario para que la sesión siga abierta al reabrir la app,
 * y el IDIOMA elegido (es / en). Los valores se mantienen también en memoria para leerlos rápido.
 */
class SesionDataStore(private val contexto: Context) : Sesion {

    private object Claves {
        val TOKEN = stringPreferencesKey("token")
        val ID = longPreferencesKey("usuario_id")
        val NOMBRE = stringPreferencesKey("usuario_nombre")
        val CORREO = stringPreferencesKey("usuario_correo")
        val RUT = stringPreferencesKey("usuario_rut")
        val ROL = stringPreferencesKey("usuario_rol")
        val RECURSO = intPreferencesKey("usuario_recurso")
        val IDIOMA = stringPreferencesKey("idioma")
    }

    override var token: String? = null
        private set
    override var usuario: Usuario? = null
        private set

    /** Se llama una vez al iniciar la app para recuperar la sesión guardada. */
    suspend fun cargar() {
        val p = contexto.almacen.data.first()
        token = p[Claves.TOKEN]
        val rol = p[Claves.ROL]?.let { runCatching { Rol.valueOf(it) }.getOrNull() }
        usuario = if (token != null && rol != null) Usuario(
            id = p[Claves.ID] ?: 0,
            nombre = p[Claves.NOMBRE].orEmpty(),
            correo = p[Claves.CORREO].orEmpty(),
            rut = p[Claves.RUT].orEmpty(),
            rol = rol,
            recursoId = p[Claves.RECURSO]
        ) else null
    }

    override suspend fun guardar(token: String, usuario: Usuario) {
        contexto.almacen.edit { p ->
            p[Claves.TOKEN] = token
            p[Claves.ID] = usuario.id
            p[Claves.NOMBRE] = usuario.nombre
            p[Claves.CORREO] = usuario.correo
            p[Claves.RUT] = usuario.rut
            p[Claves.ROL] = usuario.rol.name
            usuario.recursoId?.let { p[Claves.RECURSO] = it } ?: p.remove(Claves.RECURSO)
        }
        this.token = token
        this.usuario = usuario
    }

    override suspend fun cerrar() {
        contexto.almacen.edit { p ->
            listOf(Claves.TOKEN, Claves.NOMBRE, Claves.CORREO, Claves.RUT, Claves.ROL).forEach { p.remove(it) }
            p.remove(Claves.ID)
            p.remove(Claves.RECURSO)
        }
        token = null
        usuario = null
    }

    // ---------- Idioma (selector persistente ES / EN) ----------

    suspend fun guardarIdioma(codigo: String) {
        contexto.almacen.edit { it[Claves.IDIOMA] = codigo }
    }

    companion object {
        /** Lectura síncrona del idioma: la Activity la necesita ANTES de dibujar la interfaz. */
        fun idiomaGuardado(contexto: Context): String =
            runBlocking { contexto.almacen.data.first()[Claves.IDIOMA] } ?: "es"
    }
}
