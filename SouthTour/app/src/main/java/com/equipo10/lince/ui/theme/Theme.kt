package com.equipo10.lince.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Esquema de colores Material 3 con la identidad visual SouthTour (tema claro). */
private val EsquemaSouthTour = lightColorScheme(
    primary = CafeSouthTour,
    onPrimary = Blanco,
    primaryContainer = CafeSuave,
    onPrimaryContainer = TextoPrincipal,
    secondary = TurquesaSouthTour,
    onSecondary = Blanco,
    secondaryContainer = TurquesaSuave,      // chips seleccionados
    onSecondaryContainer = TextoPrincipal,
    tertiary = VerdeConfirmado,
    onTertiary = Blanco,
    background = FondoSouthTour,
    onBackground = TextoPrincipal,
    surface = FondoSouthTour,
    onSurface = TextoPrincipal,
    surfaceVariant = CafeSuave,
    onSurfaceVariant = TextoSecundario,
    // Las Card de Material 3 usan estos colores -> tarjetas en blanco
    surfaceContainerLowest = Blanco,
    surfaceContainerLow = Blanco,
    surfaceContainer = Blanco,
    surfaceContainerHigh = Blanco,
    surfaceContainerHighest = Blanco,
    outline = BordeSuave,
    outlineVariant = BordeSuave,
    error = AlertaSouthTour,
    onError = Blanco
)

@Composable
fun LinceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaSouthTour,
        typography = Typography,   // viene del Type.kt que genera Android Studio (no se borra)
        content = content
    )
}
