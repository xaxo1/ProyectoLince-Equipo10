package com.equipo10.lince.viewmodel

import com.equipo10.lince.Datos
import com.equipo10.lince.ReglaDispatcher
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.remoto.ApiExcepcion
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.FiltroServicios
import com.equipo10.lince.model.ListaServicios
import com.equipo10.lince.model.Reserva
import com.equipo10.lince.model.TipoServicio
import com.equipo10.lince.util.Formatos
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Pantallas de servicios: Hoy, Calendario (filtros y búsqueda) y Reserva. */
class ServiciosViewModelsTest {

    @get:Rule val regla = ReglaDispatcher()

    private val repo = mockk<RepositorioLince>()
    private val hoy = Formatos.hoy()
    private val manana = Formatos.fechaEnDias(1)

    // ---------------- Hoy ----------------

    @Test
    fun `hoy separa los servicios del dia y el proximo`() {
        every { repo.usuarioActual } returns Datos.transportista
        coEvery { repo.cantidadPendientes() } returns 1
        coEvery { repo.serviciosEntre(any(), any()) } returns Result.success(ListaServicios(listOf(
            Datos.servicio(id = 1, fecha = hoy, pasajeros = 4),
            Datos.servicio(id = 2, fecha = hoy, estado = EstadoServicio.CANCELADO, pasajeros = 9),
            Datos.servicio(id = 3, fecha = manana, estado = EstadoServicio.CANCELADO),
            Datos.servicio(id = 4, fecha = manana)
        ), desdeCache = true))
        val vm = InicioViewModel(repo)
        vm.cargar()
        val e = vm.uiState.value
        assertEquals(listOf(1, 2), e.serviciosHoy.map { it.id })
        assertEquals(4, e.proximo?.id)
        assertEquals(4, e.totalPasajeros)          // el cancelado no suma pasajeros
        assertTrue(e.desdeCache)
        assertEquals(1, e.pendientesSincronizar)
        assertEquals("Transportista", e.rol)
        assertTrue(!e.jornadaTerminada)
    }

    @Test
    fun `hoy sin sesion muestra error`() {
        every { repo.usuarioActual } returns null
        val vm = InicioViewModel(repo)
        vm.cargar()
        assertEquals("No hay una sesión iniciada", vm.uiState.value.error)
    }

    @Test
    fun `hoy con error de red muestra el mensaje`() {
        every { repo.usuarioActual } returns Datos.guia
        coEvery { repo.serviciosEntre(any(), any()) } returns Result.failure(ApiExcepcion(0, "SIN_CONEXION", "Sin conexión con el servidor"))
        val vm = InicioViewModel(repo)
        vm.cargar()
        assertEquals("Sin conexión con el servidor", vm.uiState.value.error)
    }

    @Test
    fun `hoy con error sin mensaje`() {
        every { repo.usuarioActual } returns Datos.guia
        coEvery { repo.serviciosEntre(any(), any()) } returns Result.failure(RuntimeException())
        val vm = InicioViewModel(repo)
        vm.cargar()
        assertEquals("No se pudieron cargar los servicios", vm.uiState.value.error)
    }

    // ---------------- Calendario ----------------

    private fun calendario(): ServiciosViewModel {
        coEvery { repo.serviciosEntre(any(), any()) } returns Result.success(ListaServicios(listOf(
            Datos.servicio(id = 1, fecha = hoy, tipo = TipoServicio.TRANSFER, excursion = "Transfer aeropuerto"),
            Datos.servicio(id = 2, fecha = manana, tipo = TipoServicio.EXCURSION, excursion = "Cajón del Maipo"),
            Datos.servicio(id = 3, fecha = hoy, estado = EstadoServicio.EN_CURSO),
            Datos.servicio(id = 4, fecha = hoy, estado = EstadoServicio.FINALIZADO),
            Datos.servicio(id = 5, fecha = hoy, estado = EstadoServicio.CANCELADO)
        )))
        return ServiciosViewModel(repo).also { it.cargar() }
    }

    private fun ids(vm: ServiciosViewModel) = vm.uiState.value.porFecha.values.flatten().map { it.id }.sorted()

    @Test
    fun `por defecto muestra los proximos (confirmados)`() {
        val vm = calendario()
        assertEquals(listOf(1, 2), ids(vm))
        assertEquals(5, vm.uiState.value.total)
        assertEquals(setOf(hoy, manana), vm.uiState.value.diasConServicio)
    }

    @Test
    fun `filtros por estado`() {
        val vm = calendario()
        vm.cambiarFiltro(FiltroServicios.EN_CURSO); assertEquals(listOf(3), ids(vm))
        vm.cambiarFiltro(FiltroServicios.FINALIZADOS); assertEquals(listOf(4), ids(vm))
        vm.cambiarFiltro(FiltroServicios.CANCELADOS); assertEquals(listOf(5), ids(vm))
        vm.cambiarFiltro(FiltroServicios.TODOS); assertEquals(listOf(1, 2, 3, 4, 5), ids(vm))
    }

    @Test
    fun `filtro por tipo y tocarlo de nuevo lo quita`() {
        val vm = calendario()
        vm.cambiarTipo(TipoServicio.EXCURSION)
        assertEquals(listOf(2), ids(vm))
        vm.cambiarTipo(TipoServicio.EXCURSION)
        assertNull(vm.uiState.value.filtroTipo)
        assertEquals(listOf(1, 2), ids(vm))
    }

    @Test
    fun `filtro por fecha de la cuadricula`() {
        val vm = calendario()
        vm.seleccionarFecha(manana)
        assertEquals(listOf(2), ids(vm))
        vm.seleccionarFecha(manana)
        assertNull(vm.uiState.value.fechaSeleccionada)
    }

    @Test
    fun `busqueda por texto sin importar mayusculas`() {
        val vm = calendario()
        vm.cambiarBusqueda("MAIPO")
        assertEquals(listOf(2), ids(vm))
        vm.cambiarBusqueda("WHZ1")
        assertEquals(listOf(1), ids(vm))
        vm.cambiarBusqueda("xyz")
        assertEquals(0, vm.uiState.value.cantidad)
    }

    @Test
    fun `calendario con error`() {
        coEvery { repo.serviciosEntre(any(), any()) } returns Result.failure(RuntimeException())
        val vm = ServiciosViewModel(repo).also { it.cargar() }
        assertEquals("No se pudieron cargar los servicios", vm.uiState.value.error)
    }

    // ---------------- Reserva ----------------

    @Test
    fun `reserva completa y error`() {
        val reserva = Reserva("WHZ1", "Andes", "Turner", "Santiago", 2, "contacto")
        coEvery { repo.reserva(1) } returns Result.success(reserva)
        coEvery { repo.reserva(2) } returns Result.failure(RuntimeException())
        val vm = ReservaViewModel(repo)
        vm.cargar(1)
        assertNotNull(vm.uiState.value.reserva)
        vm.cargar(2)
        assertEquals("No se pudo cargar la reserva", vm.uiState.value.error)
    }
}
