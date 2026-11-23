package com.equipo10.lince.data

import android.graphics.Bitmap
import com.equipo10.lince.Datos
import com.equipo10.lince.data.local.FotoIncidenciaDao
import com.equipo10.lince.data.local.FotoIncidenciaEntity
import com.equipo10.lince.data.local.HitoPendienteDao
import com.equipo10.lince.data.local.HitoPendienteEntity
import com.equipo10.lince.data.local.ServicioCacheDao
import com.equipo10.lince.data.local.ServicioCacheEntity
import com.equipo10.lince.data.remoto.ApiExcepcion
import com.equipo10.lince.data.remoto.BackendApi
import com.equipo10.lince.data.remoto.BloqueoDto
import com.equipo10.lince.data.remoto.CatalogosDto
import com.equipo10.lince.data.remoto.CodigoNombreDto
import com.equipo10.lince.data.remoto.ConfirmacionDto
import com.equipo10.lince.data.remoto.ConteoDto
import com.equipo10.lince.data.remoto.EstadoDescripcionDto
import com.equipo10.lince.data.remoto.GeocercaDto
import com.equipo10.lince.data.remoto.HitoSolicitud
import com.equipo10.lince.data.remoto.IncidenciaDto
import com.equipo10.lince.data.remoto.LinceApi
import com.equipo10.lince.data.remoto.LoginRespuesta
import com.equipo10.lince.data.remoto.MetaDto
import com.equipo10.lince.data.remoto.PaginaLince
import com.equipo10.lince.data.remoto.RecursoLinceDto
import com.equipo10.lince.data.remoto.ReglasDto
import com.equipo10.lince.data.remoto.RespuestaLince
import com.equipo10.lince.data.remoto.ResumenLinceDto
import com.equipo10.lince.data.remoto.TipoServicioDto
import com.equipo10.lince.data.remoto.UsuarioDto
import com.equipo10.lince.data.remoto.VehiculoDto
import com.equipo10.lince.model.EstadoDia
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.GeocercaInfo
import com.equipo10.lince.model.Rol
import com.equipo10.lince.model.SesionNoIniciadaException
import com.equipo10.lince.model.TipoHito
import com.equipo10.lince.model.TipoIncidencia
import com.equipo10.lince.model.TipoServicio
import com.equipo10.lince.model.Ubicacion
import com.equipo10.lince.util.Formatos
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

/**
 * Pruebas del REPOSITORIO con MockK: la Lince API, el backend, la sesión y Room son mocks,
 * así se prueba la lógica sin red ni base de datos (entrada -> resultado esperado).
 */
class RepositorioLinceTest {

    private val backend = mockk<BackendApi>()
    private val lince = mockk<LinceApi>()
    private val sesion = mockk<Sesion>(relaxed = true)
    private val pendientes = mockk<HitoPendienteDao>(relaxed = true)
    private val cache = mockk<ServicioCacheDao>(relaxed = true)
    private val fotos = mockk<FotoIncidenciaDao>(relaxed = true)
    private val almacen = mockk<AlmacenFotos>()
    private var sincronizacionesProgramadas = 0
    private val hoy = Formatos.hoy()

    private lateinit var repo: RepositorioLince

    @Before
    fun preparar() {
        every { sesion.usuario } returns Datos.transportista
        coEvery { pendientes.todos() } returns emptyList()
        coEvery { pendientes.delServicio(any()) } returns emptyList()
        coEvery { backend.misHitos() } returns emptyList()
        coEvery { backend.confirmaciones() } returns emptyList()
        coEvery { fotos.todas() } returns emptyList()
        repo = RepositorioLince(backend, lince, sesion, pendientes, cache, fotos, almacen,
            programarSincronizacion = { sincronizacionesProgramadas++ }, nuevoId = { "id-fijo" })
    }

    private fun pagina(vararg dtos: com.equipo10.lince.data.remoto.ServicioLinceDto, page: Int = 1, total: Int = 1) =
        PaginaLince(dtos.toList(), MetaDto(page, 100, dtos.size, total))

    // ======================= LOGIN Y SESIÓN =======================

    @Test
    fun `login correcto guarda token y usuario en la sesion`() = runTest {
        coEvery { backend.login(any()) } returns LoginRespuesta("jwt", UsuarioDto(2, "Nicolás", "t@s.cl", "1", "TRANSPORTISTA", 2))
        val r = repo.login(" t@s.cl ", "1234")
        assertTrue(r.isSuccess)
        assertEquals(Rol.TRANSPORTISTA, r.getOrNull()?.rol)
        coVerify { sesion.guardar("jwt", any()) }
    }

    @Test
    fun `login con rol desconocido usa transportista por defecto`() = runTest {
        coEvery { backend.login(any()) } returns LoginRespuesta("jwt", UsuarioDto(9, null, null, null, "OTRO", null))
        assertEquals(Rol.TRANSPORTISTA, repo.login("x@y.cl", "1").getOrNull()?.rol)
    }

    @Test
    fun `login sin token falla`() = runTest {
        coEvery { backend.login(any()) } returns LoginRespuesta(null, null)
        assertEquals("SIN_TOKEN", (repo.login("a@b.cl", "1").exceptionOrNull() as ApiExcepcion).codigo)
    }

    @Test
    fun `login sin usuario falla`() = runTest {
        coEvery { backend.login(any()) } returns LoginRespuesta("jwt", null)
        assertEquals("SIN_USUARIO", (repo.login("a@b.cl", "1").exceptionOrNull() as ApiExcepcion).codigo)
    }

    @Test
    fun `login con 401 entrega el mensaje y el requestId del servidor`() = runTest {
        coEvery { backend.login(any()) } throws Datos.errorHttp(401, "CREDENCIALES", "RUT/correo o contraseña incorrectos", "req-9")
        val e = repo.login("a@b.cl", "x").exceptionOrNull() as ApiExcepcion
        assertEquals(401, e.http)
        assertEquals("CREDENCIALES", e.codigo)
        assertEquals("req-9", e.requestId)
        assertEquals("RUT/correo o contraseña incorrectos", e.message)
    }

    @Test
    fun `sin internet el error es SIN_CONEXION`() = runTest {
        coEvery { backend.login(any()) } throws IOException("sin red")
        val e = repo.login("a@b.cl", "x").exceptionOrNull() as ApiExcepcion
        assertTrue(e.sinConexion)
    }

    @Test
    fun `cerrar sesion borra la sesion`() = runTest {
        repo.cerrarSesion()
        coVerify { sesion.cerrar() }
    }

    @Test
    fun `reglas usa el backend y queda en cache`() = runTest {
        coEvery { backend.reglas() } returns ReglasDto(100, 30.0, 20, 7)
        assertEquals(100, repo.reglas().radioDefecto)
        assertEquals(20, repo.reglas().minutosAntes)
        coVerify(exactly = 1) { backend.reglas() }
    }

    @Test
    fun `reglas por defecto si el backend falla`() = runTest {
        coEvery { backend.reglas() } throws IOException()
        assertEquals(150, repo.reglas().radioDefecto)
    }

    // ======================= SERVICIOS =======================

    @Test
    fun `servicios del transportista filtran por transportistaId y calculan el estado`() = runTest {
        val enCurso = Datos.servicioDto(id = 1)
        val finalizado = Datos.servicioDto(id = 2)
        val cancelado = Datos.servicioDto(id = 3, estado = "XX")
        val anulado = Datos.servicioDto(id = 4, statusPL = "CX")
        val libre = Datos.servicioDto(id = 5)
        val conAtraso = Datos.servicioDto(id = 6)
        coEvery { lince.servicios(any(), any(), any(), any(), any(), any(), any()) } returns
            pagina(enCurso, finalizado, cancelado, anulado, libre, conAtraso)
        coEvery { backend.misHitos() } returns listOf(
            Datos.hitoDto(1), Datos.hitoDto(2), Datos.hitoDto(2, "CHECK_OUT", "REGISTRADA"),
            Datos.hitoDto(6, resultado = "ATRASADA")
        )
        coEvery { backend.confirmaciones() } returns listOf(ConfirmacionDto(5, "CONFIRMADA"))

        val lista = repo.serviciosEntre(hoy, hoy).getOrThrow()
        val porId = lista.servicios.associateBy { it.id }
        assertFalse(lista.desdeCache)
        assertEquals(EstadoServicio.EN_CURSO, porId[1]?.estado)
        assertEquals(EstadoServicio.FINALIZADO, porId[2]?.estado)
        assertEquals(EstadoServicio.CANCELADO, porId[3]?.estado)
        assertEquals(EstadoServicio.CANCELADO, porId[4]?.estado)
        assertEquals(EstadoServicio.CONFIRMADO, porId[5]?.estado)
        assertEquals("CONFIRMADA", porId[5]?.confirmacion)
        assertTrue(porId[6]?.conAtraso == true)
        assertEquals("10:45", porId[1]?.horaCheckIn)
        coVerify { lince.servicios(1, 100, hoy, hoy, null, null, 2) }
        coVerify { cache.guardar(match { it.size == 6 }) }
    }

    @Test
    fun `la guia filtra por guiaId y se leen todas las paginas`() = runTest {
        every { sesion.usuario } returns Datos.guia
        coEvery { lince.servicios(1, any(), any(), any(), any(), any(), any()) } returns pagina(Datos.servicioDto(id = 1), total = 2)
        coEvery { lince.servicios(2, any(), any(), any(), any(), any(), any()) } returns pagina(Datos.servicioDto(id = 2), page = 2, total = 2)
        val lista = repo.serviciosEntre(hoy, hoy).getOrThrow()
        assertEquals(listOf(1, 2), lista.servicios.map { it.id })
        coVerify { lince.servicios(1, 100, hoy, hoy, null, 37, null) }
    }

    @Test
    fun `sin internet se usan los servicios guardados en Room`() = runTest {
        coEvery { lince.servicios(any(), any(), any(), any(), any(), any(), any()) } throws IOException()
        coEvery { cache.entre(hoy, hoy) } returns listOf(ServicioCacheEntity(7, hoy, Gson().toJson(Datos.servicioDto(id = 7))))
        coEvery { pendientes.todos() } returns listOf(HitoPendienteEntity("p", 7, "CHECK_IN", 0.0, 0.0, 5.0, "${hoy}T10:00:00"))
        val lista = repo.serviciosEntre(hoy, hoy).getOrThrow()
        assertTrue(lista.desdeCache)
        val s = lista.servicios.single()
        assertEquals(EstadoServicio.EN_CURSO, s.estado)   // por el check-in que espera sincronizar
        assertTrue(s.sincronizacionPendiente)
        assertEquals("10:00", s.horaCheckIn)
    }

    @Test
    fun `check-in fuera de zona no inicia el servicio y se puede reintentar`() = runTest {
        coEvery { lince.servicios(any(), any(), any(), any(), any(), any(), any()) } returns pagina(Datos.servicioDto(id = 9))
        coEvery { backend.misHitos() } returns listOf(Datos.hitoDto(9, resultado = "FUERA_DE_ZONA"))
        val s = repo.serviciosEntre(hoy, hoy).getOrThrow().servicios.single()
        assertEquals(EstadoServicio.CONFIRMADO, s.estado)
        assertEquals("FUERA_DE_ZONA", s.resultadoCheckIn)
        assertTrue(s.conAtraso)
    }

    @Test
    fun `check-in atrasado inicia el servicio`() = runTest {
        coEvery { lince.servicios(any(), any(), any(), any(), any(), any(), any()) } returns pagina(Datos.servicioDto(id = 9))
        coEvery { backend.misHitos() } returns listOf(Datos.hitoDto(9, resultado = "ATRASADA"))
        assertEquals(EstadoServicio.EN_CURSO, repo.serviciosEntre(hoy, hoy).getOrThrow().servicios.single().estado)
    }

    @Test
    fun `un check-out pendiente deja el servicio finalizado`() = runTest {
        coEvery { lince.servicios(any(), any(), any(), any(), any(), any(), any()) } returns pagina(Datos.servicioDto(id = 8))
        coEvery { pendientes.todos() } returns listOf(HitoPendienteEntity("p", 8, "CHECK_OUT", 0.0, 0.0, 5.0, "${hoy}T19:00:00"))
        assertEquals(EstadoServicio.FINALIZADO, repo.serviciosEntre(hoy, hoy).getOrThrow().servicios.single().estado)
    }

    @Test
    fun `error que no es de conexion se informa`() = runTest {
        coEvery { lince.servicios(any(), any(), any(), any(), any(), any(), any()) } throws Datos.errorHttp(400, "VALIDATION_ERROR")
        assertEquals(400, (repo.serviciosEntre(hoy, hoy).exceptionOrNull() as ApiExcepcion).http)
    }

    @Test
    fun `sin sesion no se pueden leer servicios`() = runTest {
        every { sesion.usuario } returns null
        assertTrue(repo.serviciosEntre(hoy, hoy).isFailure)
    }

    @Test
    fun `detalle de un servicio con su hito y su confirmacion`() = runTest {
        coEvery { lince.servicio(5) } returns RespuestaLince(Datos.servicioDto(id = 5))
        coEvery { backend.hitosDelServicio(5) } returns listOf(Datos.hitoDto(5))
        coEvery { backend.confirmaciones() } returns listOf(ConfirmacionDto(5, "RECHAZADA"))
        val s = repo.servicio(5).getOrThrow()
        assertEquals(EstadoServicio.EN_CURSO, s.estado)
        assertEquals("RECHAZADA", s.confirmacion)
        assertEquals(TipoServicio.TRANSFER, s.tipo)
        assertEquals("VAN · ZZ-1001", s.vehiculo)
        coVerify { cache.guardar(any()) }
    }

    @Test
    fun `detalle inexistente devuelve 404`() = runTest {
        coEvery { lince.servicio(99) } throws Datos.errorHttp(404, "NOT_FOUND")
        assertEquals(404, (repo.servicio(99).exceptionOrNull() as ApiExcepcion).http)
    }

    @Test
    fun `detalle sin datos devuelve 404`() = runTest {
        coEvery { lince.servicio(98) } returns RespuestaLince(null)
        assertEquals(404, (repo.servicio(98).exceptionOrNull() as ApiExcepcion).http)
    }

    @Test
    fun `detalle sin internet usa la cache y sin cache falla`() = runTest {
        coEvery { lince.servicio(any()) } throws IOException()
        coEvery { backend.hitosDelServicio(any()) } throws IOException()
        coEvery { backend.confirmaciones() } throws IOException()
        coEvery { cache.porId(5) } returns ServicioCacheEntity(5, hoy, Gson().toJson(Datos.servicioDto(id = 5)))
        coEvery { cache.porId(6) } returns null
        assertEquals(5, repo.servicio(5).getOrThrow().id)
        assertTrue(repo.servicio(6).isFailure)
    }

    @Test
    fun `registros del servicio juntan los enviados y los pendientes`() = runTest {
        coEvery { backend.hitosDelServicio(5) } returns listOf(Datos.hitoDto(5, resultado = "FUERA_DE_ZONA"))
        coEvery { pendientes.delServicio(5) } returns listOf(HitoPendienteEntity("p", 5, "CHECK_OUT", 0.0, 0.0, 9.0, "${hoy}T19:00:00"))
        val registros = repo.registrosDelServicio(5).getOrThrow()
        assertEquals(listOf("FUERA_DE_ZONA", "PENDIENTE"), registros.map { it.resultado })
        assertFalse(registros[0].verificado)
        assertTrue(registros[1].pendiente)
    }

    @Test
    fun `geocerca de un lugar`() = runTest {
        coEvery { backend.geocerca("Hotel Las Condes") } returns GeocercaDto(4, "Hotel Las Condes", "SCL", -33.4, -70.5, 150)
        assertEquals(150, repo.geocercaDe("Hotel Las Condes").getOrThrow().radioMetros)
    }

    @Test
    fun `confirmar y rechazar una asignacion`() = runTest {
        coEvery { backend.confirmar(5, any()) } answers { ConfirmacionDto(5, secondArg<com.equipo10.lince.data.remoto.ConfirmacionSolicitud>().estado) }
        assertEquals("CONFIRMADA", repo.responderAsignacion(5, true, null).getOrThrow())
        assertEquals("RECHAZADA", repo.responderAsignacion(5, false, "Sin vehículo").getOrThrow())
    }

    // ======================= HITOS Y SINCRONIZACIÓN =======================

    private val ubicacion = Ubicacion(-33.4146, -70.5941, 6.0)

    @Test
    fun `check-in enviado devuelve el resultado del servidor`() = runTest {
        val enviado = slot<HitoSolicitud>()
        coEvery { backend.registrarHito(capture(enviado)) } returns Datos.hitoDto(5)
        val r = repo.registrarHito(5, TipoHito.CHECK_IN, ubicacion).getOrThrow()
        assertEquals("VERIFICADA", r.resultado)
        assertEquals("id-fijo", enviado.captured.idempotencia)
        assertEquals(6.0, enviado.captured.precisionMetros, 0.0)
    }

    @Test
    fun `check-in rechazado por regla se informa`() = runTest {
        coEvery { backend.registrarHito(any()) } throws Datos.errorHttp(409, "NO_ES_HOY", "Solo el día del servicio")
        assertEquals("NO_ES_HOY", (repo.registrarHito(5, TipoHito.CHECK_IN, ubicacion).exceptionOrNull() as ApiExcepcion).codigo)
        coVerify(exactly = 0) { pendientes.guardar(any()) }
    }

    @Test
    fun `sin internet el check-in queda pendiente en Room y se programa WorkManager`() = runTest {
        coEvery { backend.registrarHito(any()) } throws IOException()
        val r = repo.registrarHito(5, TipoHito.CHECK_IN, ubicacion).getOrThrow()
        assertTrue(r.pendiente)
        assertEquals("PENDIENTE", r.resultado)
        coVerify { pendientes.guardar(match { it.idempotencia == "id-fijo" && it.servicioId == 5 }) }
        assertEquals(1, sincronizacionesProgramadas)
    }

    @Test
    fun `sincronizar envia los pendientes y los borra`() = runTest {
        coEvery { pendientes.todos() } returns listOf(
            HitoPendienteEntity("a", 5, "CHECK_IN", 0.0, 0.0, 5.0, "t"),
            HitoPendienteEntity("b", 5, "CHECK_OUT", 0.0, 0.0, 5.0, "t")
        )
        coEvery { backend.registrarHito(match { it.idempotencia == "a" }) } returns Datos.hitoDto()
        coEvery { backend.registrarHito(match { it.idempotencia == "b" }) } throws Datos.errorHttp(409, "YA_REGISTRADO")
        assertEquals(1, repo.sincronizarPendientes().getOrThrow())
        coVerify { pendientes.eliminar("a") }
        coVerify { pendientes.eliminar("b") }   // rechazado por regla: no se reintenta
    }

    @Test
    fun `si se corta internet al sincronizar se reintenta despues`() = runTest {
        coEvery { pendientes.todos() } returns listOf(HitoPendienteEntity("a", 5, "CHECK_IN", 0.0, 0.0, 5.0, "t"))
        coEvery { backend.registrarHito(any()) } throws IOException()
        assertTrue(repo.sincronizarPendientes().isFailure)
        coVerify(exactly = 0) { pendientes.eliminar(any()) }
    }

    @Test
    fun `cantidad de pendientes`() = runTest {
        coEvery { pendientes.cantidad() } returns 3
        assertEquals(3, repo.cantidadPendientes())
    }

    // ======================= INCIDENCIAS =======================

    private val incidenciaDto = IncidenciaDto(10, 5, "PASAJERO_AUSENTE", "Falta un pasajero en el hotel", 1, false, "ABIERTA",
        "2026-11-23T10:50:00", "Nicolás")

    @Test
    fun `incidencias del servicio con su foto local`() = runTest {
        coEvery { backend.incidenciasDelServicio(5) } returns listOf(incidenciaDto)
        coEvery { fotos.todas() } returns listOf(FotoIncidenciaEntity(10, "/foto.jpg"))
        val i = repo.incidencias(5).getOrThrow().single()
        assertEquals(TipoIncidencia.PASAJERO_AUSENTE, i.tipo)
        assertEquals("/foto.jpg", i.rutaFoto)
        assertEquals("2026-11-23 10:50", i.registradaEn)
    }

    @Test
    fun `todas las incidencias para coordinacion`() = runTest {
        coEvery { backend.incidencias() } returns listOf(incidenciaDto.copy(tipo = "DESCONOCIDO"))
        assertEquals(TipoIncidencia.OTRO, repo.todasLasIncidencias().getOrThrow().single().tipo)
    }

    @Test
    fun `registrar incidencia con foto guarda la foto en el telefono`() = runTest {
        val foto = mockk<Bitmap>()
        coEvery { backend.crearIncidencia(any()) } returns incidenciaDto
        coEvery { almacen.guardar(foto) } returns "/fotos/1.jpg"
        val i = repo.registrarIncidencia(5, TipoIncidencia.PASAJERO_AUSENTE, "  Falta un pasajero  ", 1, foto).getOrThrow()
        assertEquals("/fotos/1.jpg", i.rutaFoto)
        coVerify { backend.crearIncidencia(match { it.descripcion == "Falta un pasajero" }) }
        coVerify { fotos.guardar(FotoIncidenciaEntity(10, "/fotos/1.jpg")) }
    }

    @Test
    fun `registrar incidencia sin foto`() = runTest {
        coEvery { backend.crearIncidencia(any()) } returns incidenciaDto
        assertNull(repo.registrarIncidencia(5, TipoIncidencia.OTRO, "Descripción larga", null, null).getOrThrow().rutaFoto)
        coVerify(exactly = 0) { fotos.guardar(any()) }
    }

    // ======================= DISPONIBILIDAD =======================

    @Test
    fun `disponibilidad marca dias con servicio, bloqueados y libres`() = runTest {
        val manana = Formatos.fechaEnDias(1)
        val enDiez = Formatos.fechaEnDias(10)
        coEvery { lince.servicios(any(), any(), any(), any(), any(), any(), any()) } returns pagina(Datos.servicioDto(id = 1, fecha = manana))
        coEvery { backend.bloqueos() } returns listOf(BloqueoDto(3, enDiez, "Trámite"))
        val dias = repo.disponibilidad(15).getOrThrow()
        assertEquals(15, dias.size)
        assertEquals(EstadoDia.CON_SERVICIO, dias[1].estado)
        assertEquals(EstadoDia.BLOQUEADO, dias[10].estado)
        assertEquals(3L, dias[10].bloqueoId)
        assertEquals(EstadoDia.LIBRE, dias[0].estado)
    }

    @Test
    fun `bloquear y desbloquear dias`() = runTest {
        coEvery { backend.bloquear(any()) } returns BloqueoDto(1, "2026-12-05", null)
        coEvery { backend.desbloquear(1) } returns Response.success(Unit)
        coEvery { backend.desbloquear(2) } returns Response.error(404, "{}".toResponseBody("application/json".toMediaType()))
        assertTrue(repo.bloquearDia("2026-12-05").isSuccess)
        assertTrue(repo.desbloquearDia(1).isSuccess)
        assertEquals(404, (repo.desbloquearDia(2).exceptionOrNull() as ApiExcepcion).http)
    }

    @Test
    fun `bloqueo rechazado por la regla de 7 dias`() = runTest {
        coEvery { backend.bloquear(any()) } throws Datos.errorHttp(409, "SIN_ANTICIPACION", "Debes bloquear con 7 días")
        assertEquals("Debes bloquear con 7 días", repo.bloquearDia(hoy).exceptionOrNull()?.message)
    }

    // ======================= RESERVA, RESUMEN Y PERFIL =======================

    @Test
    fun `reserva completa desde la Lince API`() = runTest {
        coEvery { lince.servicio(5) } returns RespuestaLince(Datos.servicioDto(id = 5))
        coEvery { backend.hitosDelServicio(5) } returns emptyList()
        val r = repo.reserva(5).getOrThrow()
        assertEquals("WHZ5", r.numero)
        assertEquals(2, r.totalPasajeros)
        assertTrue(r.observaciones.any { it.contains("Inglés") })
    }

    @Test
    fun `resumen del mes con puntualidad y datos de Southbound`() = runTest {
        coEvery { lince.servicios(any(), any(), any(), any(), any(), any(), any()) } returns pagina(
            Datos.servicioDto(id = 1), Datos.servicioDto(id = 2), Datos.servicioDto(id = 3, estado = "XX", tipo = "EXCURSION")
        )
        coEvery { backend.misHitos() } returns listOf(
            Datos.hitoDto(1), Datos.hitoDto(1, "CHECK_OUT", "REGISTRADA"), Datos.hitoDto(2, resultado = "ATRASADA")
        )
        coEvery { lince.resumen(any(), any()) } returns RespuestaLince(ResumenLinceDto(4000, 1, listOf(ConteoDto("CF", 3413))))
        val r = repo.resumen(0).getOrThrow()
        assertEquals(3, r.misServicios)
        assertEquals(1, r.completados)
        assertEquals(1, r.cancelados)
        assertEquals(50, r.puntualidad)
        assertEquals(4000, r.totalSouthbound)
        assertEquals(3413, r.porEstadoSouthbound["CF"])
        assertEquals(1, r.porTipo[TipoServicio.EXCURSION])
        assertTrue(r.esMesActual)
    }

    @Test
    fun `resumen sin check-in no tiene puntualidad`() = runTest {
        coEvery { lince.servicios(any(), any(), any(), any(), any(), any(), any()) } returns pagina()
        coEvery { lince.resumen(any(), any()) } throws IOException()
        val r = repo.resumen(-1).getOrThrow()
        assertNull(r.puntualidad)
        assertEquals(0, r.totalSouthbound)
        assertFalse(r.esMesActual)
    }

    @Test
    fun `perfil del transportista cuenta servicios, hitos y pendientes`() = runTest {
        coEvery { lince.servicios(any(), any(), any(), any(), any(), any(), any()) } returns pagina(Datos.servicioDto(id = 1))
        coEvery { backend.misHitos() } returns listOf(Datos.hitoDto(1))
        coEvery { pendientes.cantidad() } returns 2
        val p = repo.perfil().getOrThrow()
        assertEquals(1, p.serviciosDelMes)
        assertEquals(1, p.hitosRegistrados)
        assertEquals(2, p.pendientesSincronizar)
    }

    @Test
    fun `perfil de coordinacion no consulta servicios propios`() = runTest {
        every { sesion.usuario } returns Datos.coordinador
        coEvery { pendientes.cantidad() } returns 0
        assertEquals(0, repo.perfil().getOrThrow().serviciosDelMes)
        coVerify(exactly = 0) { lince.servicios(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `perfil sin sesion falla`() = runTest {
        every { sesion.usuario } returns null
        assertTrue(repo.perfil().exceptionOrNull() is ApiExcepcion)
    }

    // ======================= COORDINACIÓN =======================

    @Test
    fun `panel, posiciones, geocercas y cambio de radio`() = runTest {
        coEvery { backend.hitosDelDia(null) } returns listOf(Datos.hitoDto(resultado = "FUERA_DE_ZONA"))
        coEvery { backend.posiciones() } returns listOf(Datos.hitoDto(tipo = "POSICION", resultado = "REGISTRADA"))
        coEvery { backend.geocercas() } returns listOf(GeocercaDto(4, "Hotel Las Condes", "SCL", -33.4, -70.5, 150))
        coEvery { backend.actualizarGeocerca(4, any()) } returns GeocercaDto(4, "Hotel Las Condes", "SCL", -33.4, -70.5, 100)
        assertEquals("FUERA_DE_ZONA", repo.panelDelDia().getOrThrow().single().resultado)
        assertEquals("POSICION", repo.ultimasPosiciones().getOrThrow().single().tipo)
        val g = repo.geocercas().getOrThrow().single()
        assertEquals(100, repo.cambiarRadio(g, 100).getOrThrow().radioMetros)
        coVerify { backend.actualizarGeocerca(4, match { it.radioMetros == 100 }) }
    }

    @Test
    fun `cambiar radio sin permiso devuelve 403`() = runTest {
        coEvery { backend.actualizarGeocerca(any(), any()) } throws Datos.errorHttp(403, "FORBIDDEN")
        val g = GeocercaInfo(4, "H", "SCL", 0.0, 0.0, 150)
        assertEquals(403, (repo.cambiarRadio(g, 100).exceptionOrNull() as ApiExcepcion).http)
    }

    // ======================= CATÁLOGO SOUTHBOUND =======================

    @Test
    fun `servicios de Southbound paginados y filtrados por tipo`() = runTest {
        coEvery { lince.servicios(2, 20, null, null, "EXCURSION", null, null) } returns
            PaginaLince(listOf(Datos.servicioDto(id = 1, tipo = "EXCURSION")), MetaDto(2, 20, 1171, 59))
        val p = repo.serviciosSouthbound(2, TipoServicio.EXCURSION).getOrThrow()
        assertEquals(2, p.pagina)
        assertEquals(59, p.totalPaginas)
        assertTrue(p.hayMas)
        assertEquals(TipoServicio.EXCURSION, p.items.single().tipo)
    }

    @Test
    fun `guias y transportistas de Southbound`() = runTest {
        coEvery { lince.guias(1, 20) } returns PaginaLince(
            listOf(RecursoLinceDto(17, "GUI-017", "Álvaro", CodigoNombreDto("SCL", "Santiago"), listOf(CodigoNombreDto("ES", "Español")), null, 4.0)),
            MetaDto(1, 20, 45, 3))
        coEvery { lince.transportistas(1, 20) } returns PaginaLince(
            listOf(RecursoLinceDto(59, "TRA-059", "Álvaro V", null, null, listOf(VehiculoDto(71, "ZZ-1071", "VAN", 8)), null)), null)
        assertEquals("Español", repo.recursosSouthbound(true, 1).getOrThrow().items.single().detalle)
        assertEquals("VAN ZZ-1071", repo.recursosSouthbound(false, 1).getOrThrow().items.single().detalle)
    }

    @Test
    fun `catalogos de la Lince API`() = runTest {
        coEvery { lince.catalogos() } returns RespuestaLince(CatalogosDto(
            oficinas = listOf(CodigoNombreDto("SCL", "Santiago")),
            tiposServicio = listOf(TipoServicioDto("TRANSFER", "TR")),
            estadosServicio = listOf(EstadoDescripcionDto("XX", "Cancelado")),
            statusPL = listOf(EstadoDescripcionDto("CX", "Cancelado"))
        ))
        val c = repo.catalogos().getOrThrow()
        assertEquals("SCL · Santiago", c.oficinas.single())
        assertEquals("TRANSFER (TR)", c.tiposServicio.single())
        assertEquals("XX" to "Cancelado", c.estadosServicio.single())
    }

    @Test
    fun `catalogos vacios fallan con 404`() = runTest {
        coEvery { lince.catalogos() } returns RespuestaLince(null)
        assertEquals(404, (repo.catalogos().exceptionOrNull() as ApiExcepcion).http)
    }

    @Test
    fun `la clave de API invalida da 401 con requestId`() = runTest {
        coEvery { lince.catalogos() } throws Datos.errorHttp(401, "UNAUTHORIZED", "API key ausente o inválida", "abc")
        val e = repo.catalogos().exceptionOrNull() as ApiExcepcion
        assertEquals("abc", e.requestId)
        assertEquals("API key ausente o inválida", e.message)
    }

    @Test
    fun `sesion no iniciada es un error controlado`() {
        assertEquals("No hay una sesión iniciada", SesionNoIniciadaException().message)
    }
}
