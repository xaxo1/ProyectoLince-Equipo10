import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // KSP: genera el código de Room (DAO y Database) al compilar
    alias(libs.plugins.ksp)
    // Kover: reporte de cobertura de las pruebas unitarias
    alias(libs.plugins.kover)
}

// Valores locales que NO se suben a GitHub (local.properties está en .gitignore):
// URL del backend, X-API-Key de la Lince API y datos del keystore para firmar.
val locales = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "com.equipo10.lince"
    // Lifecycle 2.11.0 (Clase 6) y Compose 1.11 requieren compilar con API 37
    compileSdk = 37

    defaultConfig {
        applicationId = "com.equipo10.lince"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig: la app lee estas constantes en tiempo de ejecución
        buildConfigField("String", "BACKEND_URL", "\"${locales.getProperty("lince.backend.url", "http://10.0.2.2:8080/")}\"")
        buildConfigField("String", "LINCE_API_URL", "\"https://ws.southbound.travel/lince-api/\"")
        buildConfigField("String", "LINCE_API_KEY", "\"${locales.getProperty("lince.api.key", "")}\"")
    }

    // Firma del APK release: el .jks y sus claves se leen de local.properties (fuera de Git)
    signingConfigs {
        create("release") {
            val ruta = locales.getProperty("firma.keystore")
            if (ruta != null) {
                storeFile = file(ruta)
                storePassword = locales.getProperty("firma.storePassword")
                keyAlias = locales.getProperty("firma.keyAlias")
                keyPassword = locales.getProperty("firma.keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (locales.getProperty("firma.keystore") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

// Cobertura: se mide sobre la lógica (ViewModel y Repositorio), como pide la pauta
kover {
    reports {
        filters {
            includes {
                classes("com.equipo10.lince.viewmodel.*", "com.equipo10.lince.data.RepositorioLince*", "com.equipo10.lince.util.Geocerca*")
            }
        }
        verify {
            rule { minBound(90) }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose + Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    // Íconos extendidos: los mismos del diseño de Google Stitch (Today, FlightLand, Groups...)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Clase 6: ViewModel + collectAsStateWithLifecycle()
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Persistencia local con Room (SQLite)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Red: Retrofit + Gson + log de peticiones
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // Offline: WorkManager sincroniza los hitos pendientes; DataStore guarda sesión e idioma
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    // Pruebas unitarias (JUnit + MockK + corrutinas)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    // Prueba de interfaz con Compose (se ejecuta en el emulador o teléfono)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    // Espresso 3.7+: compatible con Android 17 (las versiones antiguas fallan con InputManager)
    androidTestImplementation(libs.androidx.test.espresso.core)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
