// Archivo de configuración del proyecto (raíz).
// AGP 9 trae Kotlin integrado: ya no se usa el plugin "kotlin-android".
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
