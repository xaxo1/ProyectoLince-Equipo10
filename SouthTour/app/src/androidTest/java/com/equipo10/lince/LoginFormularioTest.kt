package com.equipo10.lince

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.equipo10.lince.ui.screens.LoginScreen
import com.equipo10.lince.ui.theme.LinceTheme
import com.equipo10.lince.viewmodel.LoginViewModel
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * COMPOSE UI TEST: el formulario de login con datos INVÁLIDOS muestra el error bajo cada campo
 * y NO deja entrar. Se ejecuta en el emulador o teléfono (Run en Android Studio o connectedDebugAndroidTest).
 */
@RunWith(AndroidJUnit4::class)
class LoginFormularioTest {

    @get:Rule
    val compose = createComposeRule()

    private var entro = false

    private fun abrirLogin() {
        compose.setContent {
            LinceTheme {
                LoginScreen(onLoginExitoso = { entro = true }, viewModel = LoginViewModel())
            }
        }
    }

    @Test
    fun formularioVacioMuestraErroresEnAmbosCampos() {
        abrirLogin()
        compose.onNodeWithTag("botonIngresar").performClick()

        compose.onNodeWithText("Ingresa tu RUT o tu correo").assertIsDisplayed()
        compose.onNodeWithText("Ingresa tu contraseña").assertIsDisplayed()
        assertFalse(entro)
    }

    @Test
    fun rutConFormatoInvalidoNoPermiteIngresar() {
        abrirLogin()
        compose.onNodeWithTag("campoIdentificador").performTextInput("123-4")
        compose.onNodeWithTag("campoClave").performTextInput("1234")
        compose.onNodeWithTag("botonIngresar").performClick()

        compose.onNodeWithText("El RUT debe tener el formato 12.345.678-9").assertIsDisplayed()
        assertFalse(entro)
    }

    @Test
    fun correoInvalidoMuestraError() {
        abrirLogin()
        compose.onNodeWithTag("campoIdentificador").performTextInput("guia@")
        compose.onNodeWithTag("campoClave").performTextInput("1234")
        compose.onNodeWithTag("botonIngresar").performClick()

        compose.onNodeWithText("El correo no tiene un formato válido").assertIsDisplayed()
        assertFalse(entro)
    }
}
