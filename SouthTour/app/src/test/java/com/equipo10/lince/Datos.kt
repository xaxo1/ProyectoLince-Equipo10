package com.equipo10.lince

import com.equipo10.lince.data.remoto.AsignacionDto
import com.equipo10.lince.data.remoto.ClienteDto
import com.equipo10.lince.data.remoto.CodigoNombreDto
import com.equipo10.lince.data.remoto.HitoDto
import com.equipo10.lince.data.remoto.PaxDto
import com.equipo10.lince.data.remoto.RecursoRefDto
import com.equipo10.lince.data.remoto.ReservaLinceDto
import com.equipo10.lince.data.remoto.ServicioLinceDto
import com.equipo10.lince.data.remoto.VehiculoDto
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.Rol
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.model.TipoServicio
import com.equipo10.lince.model.Usuario
import com.equipo10.lince.util.Formatos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.HttpException
import retrofit2.Response

/** Regla JUnit: reemplaza Dispatchers.Main (viewModelScope) por un dispatcher de pruebas. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReglaDispatcher(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/** Datos de prueba reutilizables (ficticios). */
object Datos {

    val guia = Usuario(1, "Gonzalo Orellana", "guia@southbound.cl", "15482910K", Rol.GUIA, 37)
    val transportista = Usuario(2, "Nicolás Escobar", "transportista@southbound.cl", "123456785", Rol.TRANSPORTISTA, 2)
    val coordinador = Usuario(3, "Coordinación", "coordinacion@southbound.cl", "111111111", Rol.COORDINADOR, null)

    fun servicioDto(
        id: Int = 3129,
        fecha: String = Formatos.hoy(),
        hora: String = "18:15",
        estado: String = "CF",
        statusPL: String? = "CF",
        tipo: String = "TRANSFER",
        pickup: String = "Hotel Las Condes"
    ) = ServicioLinceDto(
        id = id, codigo = "SRV-$id", srv = "TR", tipo = tipo, fecha = fecha, horaPickup = hora,
        descripcion = "Transfer $pickup → Aeropuerto", estadoServicio = estado,
        reserva = ReservaLinceDto("WHZ$id", "Turner x2", "CF"), pax = PaxDto(2, 0, 2, "2+0"),
        pickup = pickup, dropoff = "Aeropuerto Internacional SCL", notas = "Nota de prueba",
        oficina = CodigoNombreDto("SCL", "Santiago"), cliente = ClienteDto(1, "CLI-001", "Andes Travel", "Chile"),
        idiomaRequerido = CodigoNombreDto("EN", "Inglés"), guia = RecursoRefDto(37, "GUI-037", "Gonzalo"),
        transportista = RecursoRefDto(2, "TRA-002", "Nicolás"), vehiculo = VehiculoDto(1, "ZZ-1001", "VAN", 8),
        asignacion = AsignacionDto(statusPL)
    )

    fun hitoDto(servicioId: Int = 3129, tipo: String = "CHECK_IN", resultado: String = "VERIFICADA") = HitoDto(
        id = 1, idempotencia = "x", servicioId = servicioId, tipo = tipo, latitud = -33.41, longitud = -70.59,
        precisionMetros = 8.0, distanciaMetros = 20.0, radioMetros = 150, lugar = "Hotel Las Condes",
        resultado = resultado, mensaje = "ok", fechaHoraServidor = "2026-11-23T10:45:00", usuarioId = 2, usuarioNombre = "Nicolás"
    )

    fun servicio(
        id: Int = 3129,
        fecha: String = Formatos.hoy(),
        estado: EstadoServicio = EstadoServicio.CONFIRMADO,
        tipo: TipoServicio = TipoServicio.TRANSFER,
        pasajeros: Int = 2,
        excursion: String = "Transfer Hotel Las Condes → Aeropuerto",
        hora: String = "18:15"
    ) = Servicio(
        id = id, codigo = "SRV-$id", tipo = tipo, fecha = fecha, horaPickup = hora, excursion = excursion,
        agencia = "Andes Travel", numeroReserva = "WHZ$id", titularReserva = "Turner x2", puntoPickup = "Hotel Las Condes",
        dropOff = "Aeropuerto Internacional SCL", pasajeros = pasajeros, paxTexto = "2+0", estadoLince = "CF",
        statusPL = "CF", oficina = "Santiago", estado = estado
    )

    /** Error HTTP como lo devuelve Retrofit, con el cuerpo { error: { code, message, requestId } }. */
    fun errorHttp(codigo: Int, code: String = "ERROR", mensaje: String = "falló", requestId: String = "req-1"): HttpException =
        HttpException(
            Response.error<Any>(
                codigo,
                """{"error":{"code":"$code","message":"$mensaje","requestId":"$requestId"}}"""
                    .toResponseBody("application/json".toMediaType())
            )
        )
}
