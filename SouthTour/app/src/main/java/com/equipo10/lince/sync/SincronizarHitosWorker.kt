package com.equipo10.lince.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.equipo10.lince.data.ServiceLocator
import java.util.concurrent.TimeUnit

/**
 * WORKMANAGER: envía al backend los hitos que quedaron en Room sin conexión.
 * Solo corre cuando hay internet (restricción CONNECTED) y, si falla, reintenta con espera creciente.
 */
class SincronizarHitosWorker(contexto: Context, parametros: WorkerParameters) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result =
        ServiceLocator.repositorio.sincronizarPendientes().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )

    companion object {
        private const val NOMBRE = "sincronizar-hitos"

        /** Programa la sincronización; si ya había una en cola, la mantiene (no se duplica). */
        fun programar(contexto: Context) {
            val peticion = OneTimeWorkRequestBuilder<SincronizarHitosWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(contexto).enqueueUniqueWork(NOMBRE, ExistingWorkPolicy.KEEP, peticion)
        }
    }
}
