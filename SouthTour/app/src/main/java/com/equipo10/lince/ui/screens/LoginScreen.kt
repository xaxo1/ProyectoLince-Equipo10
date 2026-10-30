package com.equipo10.lince.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo10.lince.R
import com.equipo10.lince.ui.navigation.NOMBRE_APP
import com.equipo10.lince.ui.theme.ArenaSouthTour
import com.equipo10.lince.viewmodel.LoginViewModel

/** Teléfono ficticio de Coordinación Central (solo abre el marcador, no llama). */
private const val TELEFONO_COORDINACION = "+56229876543"

/** Pantalla "Iniciar sesión" (diseño de Google Stitch). */
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var claveVisible by rememberSaveable { mutableStateOf(false) }
    val contexto = LocalContext.current

    // Cuando el ViewModel avisa que el login fue exitoso, se navega a "Hoy"
    LaunchedEffect(estado.loginExitoso) {
        if (estado.loginExitoso) {
            viewModel.navegacionRealizada()
            onLoginExitoso()
        }
    }

    // Colores de los campos: relleno arena al 20 % y borde turquesa al enfocar
    val coloresCampo = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = ArenaSouthTour.copy(alpha = 0.2f),
        focusedContainerColor = ArenaSouthTour.copy(alpha = 0.2f),
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        focusedBorderColor = MaterialTheme.colorScheme.secondary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ----- Chip "Red operativa" -----
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("RED OPERATIVA ACTIVA · CHILE", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ----- Logo (moái pixel art del equipo) -----
        Surface(color = Color.White, shape = RoundedCornerShape(28.dp), shadowElevation = 2.dp) {
            Image(
                painter = painterResource(R.drawable.logo_southtour),
                contentDescription = "Logo SouthTour",
                modifier = Modifier.padding(14.dp).size(92.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(NOMBRE_APP, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            "Plataforma interna de operaciones — Guías y Transportistas",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        // ----- Formulario -----
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Usuario / Identificador", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("RUT o correo", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = estado.identificador,
                    onValueChange = viewModel::onIdentificadorChange,
                    placeholder = { Text("15.482.910-K") },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = coloresCampo,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Para transportistas de flota o guías de expedición",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(16.dp))
                Text("Contraseña", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = estado.clave,
                    onValueChange = viewModel::onClaveChange,
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { claveVisible = !claveVisible }) {
                            Icon(
                                if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = coloresCampo,
                    visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = viewModel::iniciarSesion,
                    enabled = !estado.cargando,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (estado.cargando) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Ingresar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center) {
                    Text("¿Problemas de acceso?", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = {
                        // Abre el marcador del teléfono (no requiere permisos)
                        try {
                            contexto.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$TELEFONO_COORDINACION")))
                        } catch (e: ActivityNotFoundException) {
                            // El emulador o dispositivo no tiene app de teléfono: no se hace nada
                        }
                    }) {
                        Text("Contactar a Coordinación", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // ----- Error de autenticación (banner rojo de Stitch) -----
        estado.error?.let { mensaje ->
            Spacer(Modifier.height(16.dp))
            Surface(color = MaterialTheme.colorScheme.error, shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Error, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("FALLO DE AUTENTICACIÓN", style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold)
                        Text(mensaje, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = viewModel::limpiarError,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Reintentar") }
                }
            }
        }

        // ----- Usuarios de prueba -----
        Spacer(Modifier.height(16.dp))
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("Usuarios de prueba (clave 1234)", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold)
                Text("Guía: 15.482.910-K  ·  Transportista: 12.345.678-5",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
