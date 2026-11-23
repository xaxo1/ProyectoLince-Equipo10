package com.equipo10.lince.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** Esquema de colores Material 3 con la identidad SouthTour (sistema "Atacama Fieldwork"). */
private val EsquemaSouthTour = lightColorScheme(
    primary = CafeSouthTour,
    onPrimary = Blanco,
    primaryContainer = CafeSuave,
    onPrimaryContainer = TextoPrincipal,
    secondary = TurquesaSouthTour,
    onSecondary = Blanco,
    secondaryContainer = TurquesaSuave,
    onSecondaryContainer = TextoPrincipal,
    tertiary = VerdeConfirmado,
    onTertiary = Blanco,
    background = FondoSouthTour,
    onBackground = TextoPrincipal,
    surface = FondoSouthTour,
    onSurface = TextoPrincipal,
    surfaceVariant = CafeSuave,
    onSurfaceVariant = TextoSecundario,
    surfaceContainerLowest = Blanco,
    surfaceContainerLow = Blanco,
    surfaceContainer = Blanco,
    surfaceContainerHigh = Blanco,
    surfaceContainerHighest = Blanco,
    outline = ArenaSouthTour,
    outlineVariant = ArenaSouthTour,
    error = AlertaSouthTour,
    onError = Blanco,
    errorContainer = AlertaSuave,
    onErrorContainer = TextoPrincipal
)

/** Formas del diseño: tarjetas de 16 dp y hojas de 28 dp. */
private val FormasSouthTour = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LinceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaSouthTour,
        typography = Typography,
        shapes = FormasSouthTour,
        content = content
    )
}
