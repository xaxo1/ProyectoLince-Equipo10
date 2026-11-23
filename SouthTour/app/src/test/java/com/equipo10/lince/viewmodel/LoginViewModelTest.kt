package com.equipo10.lince.viewmodel

import com.equipo10.lince.Datos
import com.equipo10.lince.ReglaDispatcher
import com.equipo10.lince.data.RepositorioLince
import com.equipo10.lince.data.remoto.ApiExcepcion
import com.equipo10.lince.model.Rol
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Validación del formulario de login y su resultado (sin red: el repositorio es un mock). */
class LoginViewModelTest {

    @get:Rule val regla = ReglaDispatcher()

    private val repo = mockk<RepositorioLince>()
    private val vm = LoginViewModel(repo)

    @Test
    fun `campos vacios muestran un error bajo cada campo y no llaman al backend`() {
        vm.iniciarSesion()
        assertEquals("Ingresa tu RUT o tu correo", vm.uiState.value.errorIdentificador)
        assertEquals("Ingresa tu contraseña", vm.uiState.value.errorClave)
        coVerify(exactly = 0) { repo.login(any(), any()) }
    }

    @Test
    fun `RUT con formato invalido`() {
        vm.onIdentificadorChange("123-4")
        vm.onClaveChange("1234")
        vm.iniciarSesion()
        assertEquals("El RUT debe tener el formato 12.345.678-9", vm.uiState.value.errorIdentificador)
    }

    @Test
    fun `correo con formato invalido`() {
        vm.onIdentificadorChange("guia@")
        vm.onClaveChange("1234")
        vm.iniciarSesion()
        assertEquals("El correo no tiene un formato válido", vm.uiState.value.errorIdentificador)
    }

    @Test
    fun `al escribir se borra el error del campo`() {
        vm.iniciarSesion()
        vm.onIdentificadorChange("1")
        vm.onClaveChange("2")
        assertNull(vm.uiState.value.errorIdentificador)
        assertNull(vm.uiState.value.errorClave)
    }

    @Test
    fun `login correcto guarda el rol para navegar`() {
        coEvery { repo.login("12.345.678-5", "1234") } returns Result.success(Datos.transportista)
        vm.onIdentificadorChange("12.345.678-5")
        vm.onClaveChange("1234")
        vm.iniciarSesion()
        assertTrue(vm.uiState.value.loginExitoso)
        assertEquals(Rol.TRANSPORTISTA, vm.uiState.value.rol)
        vm.navegacionRealizada()
        assertFalse(vm.uiState.value.loginExitoso)
    }

    @Test
    fun `credenciales incorrectas muestran el mensaje del servidor`() {
        coEvery { repo.login(any(), any()) } returns Result.failure(ApiExcepcion(401, "CREDENCIALES", "RUT/correo o contraseña incorrectos"))
        vm.onIdentificadorChange("guia@southbound.cl")
        vm.onClaveChange("mala")
        vm.iniciarSesion()
        assertEquals("RUT/correo o contraseña incorrectos", vm.uiState.value.error)
        assertFalse(vm.uiState.value.cargando)
        vm.limpiarError()
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `error sin mensaje usa un texto por defecto`() {
        coEvery { repo.login(any(), any()) } returns Result.failure(RuntimeException())
        vm.onIdentificadorChange("guia@southbound.cl")
        vm.onClaveChange("1234")
        vm.iniciarSesion()
        assertEquals("Error desconocido", vm.uiState.value.error)
    }
}
