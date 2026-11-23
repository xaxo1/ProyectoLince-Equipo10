package com.equipo10.lince.viewmodel

import com.equipo10.lince.Datos
import com.equipo10.lince.ReglaDispatcher
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.remoto.ApiExcepcion
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.GeocercaInfo
import com.equipo10.lince.model.RegistroHito
import com.equipo10.lince.model.Reglas
import com.equipo10.lince.model.TipoHito
import com.equipo10.lince.model.Ubicacion
import com.equipo10.lince.util.Formatos
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Check-in / check-out con geocerca: el GPS y el repositorio son falsos para probar cada caso. */
class DetalleServicioViewModelTest {

    @get:Rule val regla = ReglaDispatcher()

    private val repo = mockk<RepositorioLince>()
    private val hotel = GeocercaInfo(4, "Hotel Las Condes", "SCL", -33.41450, -70.59400, 150)
    private val cerca = Ubicacion(-33.41480, -70.59420, 5.0)       // ~38 m del hotel
    private var gps: Result<Ubicacion> = Result.success(cerca)

    private fun vm() = DetalleServicioViewModel(repo) { gps }

    private fun servicio(estado: EstadoServicio, fecha: String = Formatos.hoy()) = Datos.servicio(id = 5, estado = estado, fecha = fecha)

    @Before
    fun preparar() {
        every { repo.usuarioActual } returns Datos.transportista
        coEvery { repo.reglas() } returns Reglas()
        coEvery { repo.geocercaDe(any()) } returns Result.success(hotel)
        coEvery { repo.registrosDelServicio(5) } returns Result.success(emptyList())
        coEvery { repo.incidencias(5) } returns Result.success(emptyList())
    }

    @Test
    fun `cargar muestra servicio, geocerca y mensaje segun el estado`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.CONFIRMADO))
        val vm = vm().also { it.cargar(5) }
        val e = vm.uiState.value
        assertEquals(150, e.geocerca?.radioMetros)
        assertTrue(e.esTransportista)
        assertTrue(e.mensaje.contains("15 min"))
        assertFalse(e.cargando)
    }

    @Test
    fun `mensajes para servicio de otro dia, en curso, finalizado y cancelado`() {
        val casos = mapOf(
            servicio(EstadoServicio.CONFIRMADO, Formatos.fechaEnDias(2)) to "Podrás hacer check-in",
            servicio(EstadoServicio.EN_CURSO) to "en curso",
            servicio(EstadoServicio.FINALIZADO) to "finalizado",
            servicio(EstadoServicio.CANCELADO) to "cancelado"
        )
        casos.forEach { (s, texto) ->
            coEvery { repo.servicio(5) } returns Result.success(s)
            val vm = vm().also { it.cargar(5) }
            assertTrue("${s.estado}: ${vm.uiState.value.mensaje}", vm.uiState.value.mensaje.contains(texto, ignoreCase = true))
        }
    }

    @Test
    fun `error al cargar`() {
        coEvery { repo.servicio(5) } returns Result.failure(RuntimeException())
        val vm = vm().also { it.cargar(5) }
        assertEquals("No se pudo cargar el servicio", vm.uiState.value.error)
    }

    @Test
    fun `check-in verificado calcula la distancia y muestra la respuesta del servidor`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.CONFIRMADO))
        coEvery { repo.registrarHito(5, TipoHito.CHECK_IN, cerca) } returns
            Result.success(RegistroHito(TipoHito.CHECK_IN, "VERIFICADA", "Llegada verificada a 38 m", 38.0, 150, 5.0, "10:45"))
        val vm = vm().also { it.cargar(5) }
        vm.avanzarEstado()
        val e = vm.uiState.value
        assertTrue(e.distanciaActual!! in 30.0..45.0)
        assertEquals("VERIFICADA", e.ultimoRegistro?.resultado ?: "VERIFICADA")
        coVerify { repo.registrarHito(5, TipoHito.CHECK_IN, cerca) }
    }

    @Test
    fun `check-in rechazado por el servidor se muestra como alerta`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.CONFIRMADO))
        coEvery { repo.registrarHito(any(), any(), any()) } returns
            Result.success(RegistroHito(TipoHito.CHECK_IN, "FUERA_DE_ZONA", "Fuera de zona: a 900 m", 900.0, 150, 5.0, "10:45"))
        coEvery { repo.registrosDelServicio(5) } returns Result.success(listOf(
            RegistroHito(TipoHito.CHECK_IN, "FUERA_DE_ZONA", "Fuera de zona: a 900 m", 900.0, 150, 5.0, "10:45")))
        val vm = vm().also { it.cargar(5) }
        vm.avanzarEstado()
        assertEquals("FUERA_DE_ZONA", vm.uiState.value.ultimoRegistro?.resultado)
    }

    @Test
    fun `check-out cuando el servicio esta en curso`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.EN_CURSO))
        coEvery { repo.registrarHito(5, TipoHito.CHECK_OUT, any()) } returns
            Result.success(RegistroHito(TipoHito.CHECK_OUT, "REGISTRADA", "Check-out", 10.0, 150, 5.0, "19:00"))
        val vm = vm().also { it.cargar(5) }
        vm.avanzarEstado()
        coVerify { repo.registrarHito(5, TipoHito.CHECK_OUT, any()) }
    }

    @Test
    fun `un servicio finalizado no registra nada`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.FINALIZADO))
        val vm = vm().also { it.cargar(5) }
        vm.avanzarEstado()
        coVerify(exactly = 0) { repo.registrarHito(any(), any(), any()) }
    }

    @Test
    fun `sin servicio cargado no hace nada`() {
        val vm = vm()
        vm.avanzarEstado(); vm.reportarPosicion(); vm.responderAsignacion(true); vm.medirDistancia()
        coVerify(exactly = 0) { repo.registrarHito(any(), any(), any()) }
    }

    @Test
    fun `sin GPS no se registra y se avisa`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.CONFIRMADO))
        gps = Result.failure(Exception("no se otorgó el permiso de ubicación"))
        val vm = vm().also { it.cargar(5) }
        vm.avanzarEstado()
        assertEquals("Sin ubicación: no se otorgó el permiso de ubicación", vm.uiState.value.mensaje)
        assertTrue(vm.uiState.value.mensajeEsAlerta)
        coVerify(exactly = 0) { repo.registrarHito(any(), any(), any()) }
    }

    @Test
    fun `error del servidor al registrar`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.CONFIRMADO))
        coEvery { repo.registrarHito(any(), any(), any()) } returns Result.failure(ApiExcepcion(409, "NO_ES_HOY", "Solo el día del servicio"))
        val vm = vm().also { it.cargar(5) }
        vm.avanzarEstado()
        assertEquals("Solo el día del servicio", vm.uiState.value.mensaje)
        assertFalse(vm.uiState.value.procesando)
    }

    @Test
    fun `medir distancia sin registrar`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.CONFIRMADO))
        val vm = vm().also { it.cargar(5) }
        vm.medirDistancia()
        assertNotNull(vm.uiState.value.distanciaActual)
        assertEquals(5.0, vm.uiState.value.ubicacionActual?.precisionMetros ?: 0.0, 0.0)
        gps = Result.failure(Exception("GPS apagado"))
        vm.medirDistancia()
        assertEquals("Sin ubicación: GPS apagado", vm.uiState.value.mensaje)
    }

    @Test
    fun `reportar posicion del transportista`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.EN_CURSO))
        coEvery { repo.registrarHito(5, TipoHito.POSICION, any()) } returns
            Result.success(RegistroHito(TipoHito.POSICION, "REGISTRADA", "Posición reportada", null, null, 5.0, "12:00"))
        val vm = vm().also { it.cargar(5) }
        vm.reportarPosicion()
        assertEquals("Posición enviada a coordinación", vm.uiState.value.mensaje)

        coEvery { repo.registrarHito(5, TipoHito.POSICION, any()) } returns Result.failure(ApiExcepcion(409, "X", "Servicio no activo"))
        vm.reportarPosicion()
        assertEquals("Servicio no activo", vm.uiState.value.mensaje)

        gps = Result.failure(Exception("sin señal"))
        vm.reportarPosicion()
        assertEquals("Sin ubicación: sin señal", vm.uiState.value.mensaje)
    }

    @Test
    fun `confirmar y rechazar la asignacion`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.CONFIRMADO))
        coEvery { repo.responderAsignacion(5, true, null) } returns Result.success("CONFIRMADA")
        coEvery { repo.responderAsignacion(5, false, null) } returns Result.failure(RuntimeException())
        val vm = vm().also { it.cargar(5) }
        vm.responderAsignacion(true)
        assertEquals("CONFIRMADA", vm.uiState.value.servicio?.confirmacion)
        assertEquals("Asignación confirmada", vm.uiState.value.mensaje)
        vm.responderAsignacion(false)
        assertEquals("No se pudo responder", vm.uiState.value.mensaje)
    }

    @Test
    fun `rechazar la asignacion`() {
        coEvery { repo.servicio(5) } returns Result.success(servicio(EstadoServicio.CONFIRMADO))
        coEvery { repo.responderAsignacion(5, false, null) } returns Result.success("RECHAZADA")
        val vm = vm().also { it.cargar(5) }
        vm.responderAsignacion(false)
        assertEquals("Asignación rechazada", vm.uiState.value.mensaje)
        assertTrue(vm.uiState.value.mensajeEsAlerta)
    }
}
