package com.equipo10.lince.data.remoto

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API EXTERNA del cliente (Southbound): SOLO LECTURA.
 * Base: https://ws.southbound.travel/lince-api/ · cada petición lleva el header X-API-Key
 * (lo agrega el interceptor en Red.kt). Las listas vienen paginadas en { data, meta }.
 */
interface LinceApi {

    @GET("api/v1/servicios")
    suspend fun servicios(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100,          // máximo 100
        @Query("fechaDesde") fechaDesde: String? = null, // YYYY-MM-DD
        @Query("fechaHasta") fechaHasta: String? = null,
        @Query("tipo") tipo: String? = null,             // TRANSFER, GUIDED_SERVICE, EXCURSION
        @Query("guiaId") guiaId: Int? = null,
        @Query("transportistaId") transportistaId: Int? = null
    ): PaginaLince<ServicioLinceDto>

    @GET("api/v1/servicios/{id}")
    suspend fun servicio(@Path("id") id: Int): RespuestaLince<ServicioLinceDto>

    @GET("api/v1/guias")
    suspend fun guias(@Query("page") page: Int = 1, @Query("pageSize") pageSize: Int = 20): PaginaLince<RecursoLinceDto>

    @GET("api/v1/transportistas")
    suspend fun transportistas(@Query("page") page: Int = 1, @Query("pageSize") pageSize: Int = 20): PaginaLince<RecursoLinceDto>

    @GET("api/v1/catalogos")
    suspend fun catalogos(): RespuestaLince<CatalogosDto>

    @GET("api/v1/resumen")
    suspend fun resumen(
        @Query("fechaDesde") fechaDesde: String? = null,
        @Query("fechaHasta") fechaHasta: String? = null
    ): RespuestaLince<ResumenLinceDto>
}

// ---------- Respuestas de la Lince API (Gson: todos los campos opcionales) ----------

data class RespuestaLince<T>(val data: T? = null)

data class PaginaLince<T>(val data: List<T>? = null, val meta: MetaDto? = null)

data class MetaDto(val page: Int = 1, val pageSize: Int = 0, val total: Int = 0, val totalPages: Int = 0)

data class ServicioLinceDto(
    val id: Int = 0,
    val codigo: String? = null,
    val srv: String? = null,
    val tipo: String? = null,
    val fecha: String? = null,
    val horaPickup: String? = null,
    val descripcion: String? = null,
    val estadoServicio: String? = null,   // CF, RQ, WL, XX
    val reserva: ReservaLinceDto? = null,
    val pax: PaxDto? = null,
    val pickup: String? = null,
    val dropoff: String? = null,
    val notas: String? = null,
    val oficina: CodigoNombreDto? = null,
    val cliente: ClienteDto? = null,
    val idiomaRequerido: CodigoNombreDto? = null,
    val guia: RecursoRefDto? = null,
    val transportista: RecursoRefDto? = null,
    val vehiculo: VehiculoDto? = null,
    val asignacion: AsignacionDto? = null  // statusPL: CF, RQ, DL, CX o null
)

data class ReservaLinceDto(val referencia: String? = null, val nombre: String? = null, val estado: String? = null)
data class PaxDto(val adultos: Int = 0, val ninos: Int = 0, val total: Int = 0, val texto: String? = null)
data class CodigoNombreDto(val codigo: String? = null, val nombre: String? = null)
data class ClienteDto(val id: Int = 0, val codigo: String? = null, val nombre: String? = null, val pais: String? = null)
data class RecursoRefDto(val id: Int = 0, val codigo: String? = null, val nombre: String? = null)
data class VehiculoDto(val id: Int = 0, val patente: String? = null, val tipo: String? = null, val capacidad: Int = 0)
data class AsignacionDto(val statusPL: String? = null, val actualizadoEn: String? = null)

data class RecursoLinceDto(
    val id: Int = 0,
    val codigo: String? = null,
    val nombre: String? = null,
    val oficina: CodigoNombreDto? = null,
    val idiomas: List<CodigoNombreDto>? = null,
    val vehiculos: List<VehiculoDto>? = null,
    val evaluacionPromedio: Double? = null,
    val activo: Boolean = true
)

data class CatalogosDto(
    val oficinas: List<CodigoNombreDto>? = null,
    val idiomas: List<CodigoNombreDto>? = null,
    val tiposServicio: List<TipoServicioDto>? = null,
    val estadosServicio: List<EstadoDescripcionDto>? = null,
    val statusPL: List<EstadoDescripcionDto>? = null
)

data class TipoServicioDto(val tipo: String? = null, val srv: String? = null)
data class EstadoDescripcionDto(val codigo: String? = null, val descripcion: String? = null)

data class ResumenLinceDto(
    val totalServicios: Int = 0,
    val totalPax: Int = 0,
    val porEstadoServicio: List<ConteoDto>? = null,
    val porStatusPL: List<ConteoDto>? = null,
    val porTipo: List<ConteoDto>? = null,
    val porOficina: List<ConteoDto>? = null
)

data class ConteoDto(val clave: String? = null, val servicios: Int = 0, val pax: Int = 0)
