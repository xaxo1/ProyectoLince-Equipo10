package com.equipo10.lince.data.remoto

import com.equipo10.lince.BuildConfig
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Crea los dos clientes Retrofit: uno para la Lince API (X-API-Key) y otro para el backend (JWT). */
object Red {

    private fun cliente(vararg interceptores: Interceptor): OkHttpClient {
        val log = HttpLoggingInterceptor().apply {
            // Solo en debug y sin cuerpos: así la X-API-Key y el token no quedan en el log
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .apply { interceptores.forEach { addInterceptor(it) } }
            .addInterceptor(log)
            .build()
    }

    fun linceApi(): LinceApi = Retrofit.Builder()
        .baseUrl(BuildConfig.LINCE_API_URL)
        .client(cliente(Interceptor { cadena ->
            // La clave viene de local.properties -> BuildConfig (nunca está escrita en el código)
            cadena.proceed(cadena.request().newBuilder().header("X-API-Key", BuildConfig.LINCE_API_KEY).build())
        }))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LinceApi::class.java)

    /** [token] se consulta en cada petición: después del login ya viene con el JWT. */
    fun backendApi(token: () -> String?): BackendApi = Retrofit.Builder()
        .baseUrl(BuildConfig.BACKEND_URL)
        .client(cliente(Interceptor { cadena ->
            val peticion = cadena.request().newBuilder()
            token()?.let { peticion.header("Authorization", "Bearer $it") }
            cadena.proceed(peticion.build())
        }))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BackendApi::class.java)
}

/**
 * Error de red traducido para la interfaz: código HTTP, código de negocio, mensaje y requestId
 * (el requestId permite rastrear el error en los logs del servidor).
 */
class ApiExcepcion(
    val http: Int,
    val codigo: String,
    mensaje: String,
    val requestId: String? = null
) : Exception(mensaje) {
    /** true si el problema fue de conexión (sin internet o servidor caído). */
    val sinConexion: Boolean get() = http == 0
}

/** Convierte cualquier excepción de Retrofit/OkHttp en [ApiExcepcion]. */
fun Throwable.comoApiExcepcion(): ApiExcepcion = when (this) {
    is ApiExcepcion -> this
    is HttpException -> {
        val cuerpo = runCatching { response()?.errorBody()?.string() }.getOrNull()
        val error = runCatching { Gson().fromJson(cuerpo, ErrorEnvoltorio::class.java)?.error }.getOrNull()
        val mensaje = error?.message ?: when (code()) {
            400 -> "Solicitud inválida"
            401 -> "Sesión vencida o API key inválida"
            403 -> "No tienes permiso para esta operación"
            404 -> "No encontrado"
            else -> "Error del servidor (${code()})"
        }
        ApiExcepcion(code(), error?.code ?: "HTTP_${code()}", mensaje, error?.requestId)
    }
    is IOException -> ApiExcepcion(0, "SIN_CONEXION", "Sin conexión con el servidor")
    else -> ApiExcepcion(-1, "DESCONOCIDO", message ?: "Error desconocido")
}
