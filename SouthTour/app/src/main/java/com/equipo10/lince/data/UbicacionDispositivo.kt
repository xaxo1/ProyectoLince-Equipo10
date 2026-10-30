package com.equipo10.lince.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.equipo10.lince.model.Ubicacion
import com.equipo10.lince.model.UbicacionNoDisponibleException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * RECURSO NATIVO: GPS del dispositivo (LocationManager de Android, sin librerías extra).
 * Se usa para registrar dónde se hizo el check-in y el check-out.
 * El permiso lo pide la pantalla; aquí solo se verifica antes de leer la ubicación.
 */
object UbicacionDispositivo {

    private const val ESPERA_MAXIMA_MS = 10_000L

    private lateinit var contexto: Context

    fun inicializar(context: Context) {
        contexto = context.applicationContext
    }

    fun tienePermiso(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED }

    /** Pide UNA lectura de ubicación; si tarda más de 10 s usa la última conocida. */
    @SuppressLint("MissingPermission") // el permiso se revisa con tienePermiso()
    suspend fun obtenerUbicacion(): Result<Ubicacion> = runCatching {
        if (!tienePermiso()) throw UbicacionNoDisponibleException("no se otorgó el permiso de ubicación")

        val gestor = contexto.getSystemService(LocationManager::class.java)
        if (!LocationManagerCompat.isLocationEnabled(gestor)) {
            throw UbicacionNoDisponibleException("la ubicación del teléfono está desactivada")
        }
        val proveedor = when {
            gestor.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            gestor.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> throw UbicacionNoDisponibleException("no hay un proveedor de ubicación activo")
        }

        val lectura: Location? = withTimeoutOrNull(ESPERA_MAXIMA_MS) {
            // Convierte el callback de Android en una función suspend
            suspendCancellableCoroutine { continuacion ->
                val cancelar = CancellationSignal()
                continuacion.invokeOnCancellation { cancelar.cancel() }
                LocationManagerCompat.getCurrentLocation(
                    gestor, proveedor, cancelar, ContextCompat.getMainExecutor(contexto)
                ) { ubicacion -> if (continuacion.isActive) continuacion.resume(ubicacion) }
            }
        }
        val ubicacion = lectura ?: gestor.getLastKnownLocation(proveedor)
            ?: throw UbicacionNoDisponibleException("no se pudo obtener la señal GPS")

        Ubicacion(ubicacion.latitude, ubicacion.longitude)
    }
}
