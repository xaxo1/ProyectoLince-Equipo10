package com.equipo10.lince.data.remoto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * BACKEND PROPIO del equipo (Spring Boot): aquí se ESCRIBE todo lo que la Lince API no permite:
 * hitos de check-in/out, incidencias, bloqueos y confirmaciones. Usa JWT (header Authorization).
 */
interface BackendApi {

    @POST("api/auth/login")
    suspend fun login(@Body solicitud: LoginSolicitud): LoginRespuesta

    @GET("api/reglas")
    suspend fun reglas(): ReglasDto

    // ----- Hitos -----
    @POST("api/hitos")
    suspend fun registrarHito(@Body solicitud: HitoSolicitud): HitoDto

    @GET("api/hitos/mios")
    suspend fun misHitos(): List<HitoDto>

    @GET("api/hitos/servicio/{servicioId}")
    suspend fun hitosDelServicio(@Path("servicioId") servicioId: Int): List<HitoDto>

    // ----- Geocercas -----
    @GET("api/geocercas")
    suspend fun geocercas(): List<GeocercaDto>

    @GET("api/geocercas/buscar")
    suspend fun geocerca(@Query("nombre") nombre: String): GeocercaDto

    @PUT("api/geocercas/{id}")
    suspend fun actualizarGeocerca(@Path("id") id: Long, @Body solicitud: GeocercaSolicitud): GeocercaDto

    // ----- Incidencias -----
    @GET("api/incidencias")
    suspend fun incidencias(): List<IncidenciaDto>

    @GET("api/incidencias/servicio/{servicioId}")
    suspend fun incidenciasDelServicio(@Path("servicioId") servicioId: Int): List<IncidenciaDto>

    @POST("api/incidencias")
    suspend fun crearIncidencia(@Body solicitud: IncidenciaSolicitud): IncidenciaDto

    // ----- Disponibilidad -----
    @GET("api/disponibilidad")
    suspend fun bloqueos(): List<BloqueoDto>

    @POST("api/disponibilidad")
    suspend fun bloquear(@Body solicitud: BloqueoSolicitud): BloqueoDto

    @DELETE("api/disponibilidad/{id}")
    suspend fun desbloquear(@Path("id") id: Long): Response<Unit>

    // ----- Confirmaciones -----
    @GET("api/confirmaciones")
    suspend fun confirmaciones(): List<ConfirmacionDto>

    @PUT("api/confirmaciones/{servicioId}")
    suspend fun confirmar(@Path("servicioId") servicioId: Int, @Body solicitud: ConfirmacionSolicitud): ConfirmacionDto

    // ----- Coordinación (solo rol COORDINADOR) -----
    @GET("api/coordinacion/hitos")
    suspend fun hitosDelDia(@Query("fecha") fecha: String? = null): List<HitoDto>

    @GET("api/coordinacion/posiciones")
    suspend fun posiciones(): List<HitoDto>
}

// ---------- DTOs del backend propio ----------

data class LoginSolicitud(val identificador: String, val clave: String)
data class LoginRespuesta(val token: String? = null, val usuario: UsuarioDto? = null)
data class UsuarioDto(
    val id: Long = 0, val nombre: String? = null, val correo: String? = null, val rut: String? = null,
    val rol: String? = null, val linceRecursoId: Int? = null
)

data class ReglasDto(
    val radioDefecto: Int = 150, val precisionMaxima: Double = 50.0,
    val minutosAntes: Int = 15, val diasAnticipacion: Int = 7
)

data class HitoSolicitud(
    val idempotencia: String,
    val servicioId: Int,
    val tipo: String,                 // CHECK_IN, CHECK_OUT, POSICION
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Double,
    val fechaHoraDispositivo: String
)

data class HitoDto(
    val id: Long = 0,
    val idempotencia: String? = null,
    val servicioId: Int = 0,
    val tipo: String? = null,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val precisionMetros: Double = 0.0,
    val distanciaMetros: Double? = null,
    val radioMetros: Int? = null,
    val lugar: String? = null,
    val resultado: String? = null,    // VERIFICADA, FUERA_DE_ZONA, ATRASADA, GPS_IMPRECISO, REGISTRADA
    val mensaje: String? = null,
    val fechaHoraServidor: String? = null,
    val usuarioId: Long = 0,
    val usuarioNombre: String? = null
)

data class GeocercaDto(
    val id: Long = 0, val nombre: String? = null, val oficina: String? = null,
    val latitud: Double = 0.0, val longitud: Double = 0.0, val radioMetros: Int = 150
)

data class GeocercaSolicitud(
    val nombre: String, val oficina: String, val latitud: Double, val longitud: Double, val radioMetros: Int
)

data class IncidenciaSolicitud(
    val servicioId: Int, val tipo: String, val descripcion: String, val pasajerosPresentes: Int?
)

data class IncidenciaDto(
    val id: Long = 0, val servicioId: Int = 0, val tipo: String? = null, val descripcion: String? = null,
    val pasajerosPresentes: Int? = null, val automatica: Boolean = false, val estado: String? = null,
    val fechaHora: String? = null, val usuarioNombre: String? = null
)

data class BloqueoSolicitud(val fecha: String, val motivo: String?)
data class BloqueoDto(val id: Long = 0, val fecha: String? = null, val motivo: String? = null)

data class ConfirmacionSolicitud(val estado: String, val motivo: String?)
data class ConfirmacionDto(val servicioId: Int = 0, val estado: String? = null, val motivo: String? = null)

/** Cuerpo de error de ambas APIs: { "error": { "code", "message", "requestId" } }. */
data class ErrorEnvoltorio(val error: ErrorDto? = null)
data class ErrorDto(val code: String? = null, val message: String? = null, val requestId: String? = null)
