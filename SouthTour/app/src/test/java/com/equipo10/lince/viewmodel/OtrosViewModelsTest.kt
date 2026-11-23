package com.equipo10.lince.viewmodel

import android.graphics.Bitmap
import com.equipo10.lince.Datos
import com.equipo10.lince.ReglaDispatcher
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.remoto.ApiExcepcion
import com.equipo10.lince.model.Catalogos
import com.equipo10.lince.model.DiaDisponibilidad
import com.equipo10.lince.model.EstadoDia
import com.equipo10.lince.model.GeocercaInfo
import com.equipo10.lince.model.HitoPanel
import com.equipo10.lince.model.Incidencia
import com.equipo10.lince.model.Pagina
import com.equipo10.lince.model.Perfil
import com.equipo10.lince.model.RecursoSouthbound
import com.equipo10.lince.model.ResumenMensual
import com.equipo10.lince.model.TipoIncidencia
import com.equipo10.lince.model.TipoServicio
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Formulario de incidencia, disponibilidad, resumen, perfil, coordinación y catálogo Southbound. */
class OtrosViewModelsTest {

    @get:Rule val regla = ReglaDispatcher()

    private val repo = mockk<RepositorioLince>(relaxUnitFun = true)

    // ---------------- Formulario de incidencia ----------------

    private fun formulario(): IncidenciaViewModel {
        coEvery { repo.servicio(5) } returns Result.success(Datos.servicio(id = 5, pasajeros = 12))
        return IncidenciaViewModel(repo).also { it.cargar(5) }
    }

    @Test
    fun `formulario vacio marca los tres campos`() {
        val vm = formulario()
        vm.guardar()
        val e = vm.uiState.value
        assertEquals("Selecciona el tipo de incidencia", e.errorTipo)
        assertEquals("La descripción es obligatoria", e.errorDescripcion)
        assertEquals("Indica cuántos pasajeros están presentes", e.errorPasajeros)
        assertEquals("Revisa los campos marcados en rojo", e.aviso)
    }

    @Test
    fun `reglas de descripcion y pasajeros`() {
        val vm = formulario()
        vm.onTipoChange(TipoIncidencia.PASAJERO_AUSENTE)
        vm.onDescripcionChange("corto")
        vm.onPasajerosChange("doce")
        vm.guardar()
        assertEquals("Describe lo ocurrido con al menos 10 caracteres", vm.uiState.value.errorDescripcion)
        assertEquals("Ingresa solo números", vm.uiState.value.errorPasajeros)

        vm.onPasajerosChange("13")
        vm.guardar()
        assertEquals("Debe ser un número entre 0 y 12 (pasajeros de la reserva)", vm.uiState.value.errorPasajeros)

        vm.onPasajerosChange("12")
        vm.guardar()
        assertEquals("Si falta un pasajero, los presentes deben ser menos de 12", vm.uiState.value.errorPasajeros)
    }

    @Test
    fun `la descripcion no pasa de 300 caracteres`() {
        val vm = formulario()
        vm.onDescripcionChange("a".repeat(301))
        assertEquals("", vm.uiState.value.descripcion)
    }

    @Test
    fun `incidencia valida se guarda con foto`() {
        val foto = mockk<Bitmap>()
        val guardada = Incidencia(1, 5, TipoIncidencia.PASAJERO_AUSENTE, "Falta un pasajero en el hotel", 11, false, "ABIERTA", "2026-11-23 10:50", "Nicolás")
        coEvery { repo.registrarIncidencia(5, TipoIncidencia.PASAJERO_AUSENTE, any(), 11, foto) } returns Result.success(guardada)
        val vm = formulario()
        vm.onTipoChange(TipoIncidencia.PASAJERO_AUSENTE)
        vm.onDescripcionChange("Falta un pasajero en el hotel")
        vm.onPasajerosChange(" 11 ")
        vm.onFotoTomada(foto)
        vm.onFotoTomada(null)              // cancelar la cámara no borra la foto
        assertNotNull(vm.uiState.value.foto)
        vm.guardar()
        assertEquals(guardada, vm.uiState.value.guardada)
    }

    @Test
    fun `error al guardar, quitar foto, aviso y servicio inexistente`() {
        coEvery { repo.registrarIncidencia(any(), any(), any(), any(), any()) } returns Result.failure(RuntimeException())
        val vm = formulario()
        vm.onTipoChange(TipoIncidencia.OTRO)
        vm.onDescripcionChange("Problema con el equipaje")
        vm.onPasajerosChange("2")
        vm.onFotoTomada(mockk())
        vm.quitarFoto()
        assertNull(vm.uiState.value.foto)
        vm.guardar()
        assertEquals("No se pudo guardar", vm.uiState.value.aviso)
        vm.mostrarAviso("Sin cámara")
        assertEquals("Sin cámara", vm.uiState.value.aviso)

        coEvery { repo.servicio(9) } returns Result.failure(RuntimeException())
        val otro = IncidenciaViewModel(repo).also { it.cargar(9) }
        assertEquals("No se pudo cargar el servicio", otro.uiState.value.error)
        otro.guardar()   // sin servicio no hace nada
    }

    // ---------------- Disponibilidad ----------------

    private val dias = listOf(
        DiaDisponibilidad("2026-11-23", 0, EstadoDia.CON_SERVICIO, "Transfer"),
        DiaDisponibilidad("2026-12-05", 12, EstadoDia.BLOQUEADO, bloqueoId = 3),
        DiaDisponibilidad("2026-12-06", 13, EstadoDia.LIBRE)
    )

    @Test
    fun `disponibilidad cuenta bloqueados y permite bloquear y desbloquear`() {
        coEvery { repo.disponibilidad(any()) } returns Result.success(dias)
        coEvery { repo.bloquearDia("2026-12-06", null) } returns Result.success(Unit)
        coEvery { repo.desbloquearDia(3) } returns Result.success(Unit)
        val vm = DisponibilidadViewModel(repo).also { it.cargar() }
        assertEquals(1, vm.uiState.value.bloqueados)
        vm.bloquear("2026-12-06")
        assertTrue(vm.uiState.value.mensaje.startsWith("Día bloqueado"))
        vm.desbloquear(dias[1])
        assertTrue(vm.uiState.value.mensaje.startsWith("Día disponible nuevamente"))
        vm.desbloquear(dias[2])     // sin bloqueo: no hace nada
        coVerify(exactly = 1) { repo.desbloquearDia(any()) }
    }

    @Test
    fun `errores de disponibilidad`() {
        coEvery { repo.disponibilidad(any()) } returns Result.failure(RuntimeException())
        coEvery { repo.bloquearDia(any(), any()) } returns Result.failure(ApiExcepcion(409, "SIN_ANTICIPACION", "Debes bloquear con al menos 7 días"))
        coEvery { repo.desbloquearDia(any()) } returns Result.failure(RuntimeException())
        val vm = DisponibilidadViewModel(repo).also { it.cargar() }
        assertEquals("No se pudo cargar la disponibilidad", vm.uiState.value.error)
        vm.bloquear("2026-11-24")
        assertEquals("Debes bloquear con al menos 7 días", vm.uiState.value.mensaje)
        assertTrue(vm.uiState.value.mensajeEsAlerta)
        vm.desbloquear(dias[1])
        assertEquals("No se pudo desbloquear", vm.uiState.value.mensaje)
        coEvery { repo.bloquearDia(any(), any()) } returns Result.failure(RuntimeException())
        vm.bloquear("2026-11-24")
        assertEquals("No se pudo bloquear", vm.uiState.value.mensaje)
    }

    // ---------------- Resumen ----------------

    private fun resumen(mes: String) = ResumenMensual(mes, true, 3, 1, 1, 50, mapOf(TipoServicio.TRANSFER to 3), 4000, mapOf("CF" to 10), emptyList())

    @Test
    fun `resumen navega entre meses sin pasar del actual`() {
        coEvery { repo.resumen(0) } returns Result.success(resumen("Noviembre"))
        coEvery { repo.resumen(-1) } returns Result.success(resumen("Octubre"))
        val vm = ResumenViewModel(repo).also { it.cargar() }
        assertEquals("Noviembre", vm.uiState.value.resumen?.mes)
        vm.mesSiguiente()                       // no se puede ir al futuro
        assertEquals(0, vm.uiState.value.desplazamientoMes)
        vm.mesAnterior()
        assertEquals("Octubre", vm.uiState.value.resumen?.mes)
        vm.mesSiguiente()
        assertEquals(0, vm.uiState.value.desplazamientoMes)
        vm.mesAnterior(); vm.volverAlMesActual()
        assertEquals(0, vm.uiState.value.desplazamientoMes)
    }

    @Test
    fun `resumen con error`() {
        coEvery { repo.resumen(any()) } returns Result.failure(RuntimeException())
        val vm = ResumenViewModel(repo).also { it.cargar() }
        assertEquals("No se pudo cargar el resumen", vm.uiState.value.error)
    }

    // ---------------- Perfil ----------------

    @Test
    fun `perfil, error y cerrar sesion`() {
        coEvery { repo.perfil() } returns Result.success(Perfil(Datos.guia, serviciosDelMes = 9, hitosRegistrados = 2, pendientesSincronizar = 0))
        val vm = PerfilViewModel(repo).also { it.cargar() }
        assertEquals(9, vm.uiState.value.perfil?.serviciosDelMes)
        var cerrada = false
        vm.cerrarSesion { cerrada = true }
        assertTrue(cerrada)
        coVerify { repo.cerrarSesion() }

        coEvery { repo.perfil() } returns Result.failure(RuntimeException())
        vm.cargar()
        assertEquals("No se pudo cargar el perfil", vm.uiState.value.error)
    }

    // ---------------- Coordinación ----------------

    private val hito = HitoPanel(5, "Nicolás", "CHECK_IN", "VERIFICADA", "ok", "Hotel", 20.0, 0.0, 0.0, "10:45")
    private val geocerca = GeocercaInfo(4, "Hotel Las Condes", "SCL", 0.0, 0.0, 150)

    @Test
    fun `panel de coordinacion con conteos y cambio de radio`() {
        coEvery { repo.panelDelDia(null) } returns Result.success(listOf(hito, hito.copy(resultado = "ATRASADA"), hito.copy(tipo = "CHECK_OUT", resultado = "REGISTRADA")))
        coEvery { repo.ultimasPosiciones() } returns Result.success(listOf(hito.copy(tipo = "POSICION")))
        coEvery { repo.todasLasIncidencias() } returns Result.success(emptyList())
        coEvery { repo.geocercas() } returns Result.success(listOf(geocerca, geocerca.copy(id = 5)))
        coEvery { repo.cambiarRadio(geocerca, 100) } returns Result.success(geocerca.copy(radioMetros = 100))
        val vm = CoordinacionViewModel(repo).also { it.cargar() }
        assertEquals(1, vm.uiState.value.verificados)
        assertEquals(1, vm.uiState.value.rechazados)
        assertEquals(1, vm.uiState.value.posiciones.size)
        vm.cambiarRadio(geocerca, 100)
        assertEquals(100, vm.uiState.value.geocercas.first().radioMetros)
        assertEquals(150, vm.uiState.value.geocercas.last().radioMetros)
        assertEquals("Radio de Hotel Las Condes: 100 m", vm.uiState.value.mensaje)
    }

    @Test
    fun `errores del panel de coordinacion`() {
        coEvery { repo.panelDelDia(null) } returns Result.failure(ApiExcepcion(403, "FORBIDDEN", "Tu rol no tiene permiso"))
        val vm = CoordinacionViewModel(repo).also { it.cargar() }
        assertEquals("Tu rol no tiene permiso", vm.uiState.value.error)
        coEvery { repo.cambiarRadio(any(), any()) } returns Result.failure(RuntimeException())
        vm.cambiarRadio(geocerca, 50)
        assertEquals("No se pudo cambiar el radio", vm.uiState.value.mensaje)
    }

    // ---------------- Catálogo Southbound (Lince API) ----------------

    @Test
    fun `servicios de Southbound con paginacion y filtro por tipo`() {
        val s1 = Datos.servicio(id = 1)
        val s2 = Datos.servicio(id = 2)
        coEvery { repo.serviciosSouthbound(1, null) } returns Result.success(Pagina(listOf(s1), 1, 2, 2))
        coEvery { repo.serviciosSouthbound(2, null) } returns Result.success(Pagina(listOf(s2), 2, 2, 2))
        coEvery { repo.serviciosSouthbound(1, TipoServicio.EXCURSION) } returns Result.success(Pagina(listOf(s2), 1, 1, 1))
        val vm = SouthboundViewModel(repo)
        vm.cargarPagina(1)
        assertTrue(vm.uiState.value.hayMas)
        vm.cargarMas()
        assertEquals(listOf(1, 2), vm.uiState.value.servicios.map { it.id })
        assertFalse(vm.uiState.value.hayMas)
        vm.cargarMas()                       // ya no hay más páginas
        vm.cambiarTipo(TipoServicio.EXCURSION)
        assertEquals(listOf(2), vm.uiState.value.servicios.map { it.id })
    }

    @Test
    fun `guias, transportistas y catalogos`() {
        val recurso = RecursoSouthbound(17, "GUI-017", "Álvaro", "Santiago", "Español", 4.0)
        coEvery { repo.recursosSouthbound(true, 1) } returns Result.success(Pagina(listOf(recurso), 1, 3, 45))
        coEvery { repo.recursosSouthbound(true, 2) } returns Result.success(Pagina(listOf(recurso.copy(id = 18)), 2, 3, 45))
        coEvery { repo.recursosSouthbound(false, 1) } returns Result.success(Pagina(listOf(recurso.copy(id = 59)), 1, 1, 60))
        coEvery { repo.catalogos() } returns Result.success(Catalogos(listOf("SCL"), listOf("TRANSFER"), listOf("CF" to "Confirmado"), emptyList()))
        val vm = SouthboundViewModel(repo)
        vm.cambiarPestana(PestanaSouthbound.GUIAS)
        vm.cargarMas()
        assertEquals(2, vm.uiState.value.recursos.size)
        vm.cambiarPestana(PestanaSouthbound.TRANSPORTISTAS)
        assertEquals(59, vm.uiState.value.recursos.single().id)
        vm.cambiarPestana(PestanaSouthbound.CATALOGOS)
        assertEquals(listOf("SCL"), vm.uiState.value.catalogos?.oficinas)
    }

    @Test
    fun `error de la Lince API muestra el requestId`() {
        coEvery { repo.serviciosSouthbound(any(), any()) } returns Result.failure(ApiExcepcion(401, "UNAUTHORIZED", "API key inválida", "req-77"))
        coEvery { repo.recursosSouthbound(any(), any()) } returns Result.failure(RuntimeException())
        coEvery { repo.catalogos() } returns Result.failure(RuntimeException("caído"))
        val vm = SouthboundViewModel(repo)
        vm.cargarPagina(1)
        assertEquals("API key inválida (requestId: req-77)", vm.uiState.value.error)
        vm.cambiarPestana(PestanaSouthbound.GUIAS)
        assertEquals("Error al consultar la Lince API", vm.uiState.value.error)
        vm.cambiarPestana(PestanaSouthbound.CATALOGOS)
        assertEquals("caído", vm.uiState.value.error)
    }
}
