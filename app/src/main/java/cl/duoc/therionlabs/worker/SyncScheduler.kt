package cl.duoc.therionlabs.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object SyncScheduler {

    private const val TRABAJO_SYNC =
        "therionlabs_sync_prospectos"

    // Sincronización automática
    fun solicitarSincronizacion(context: Context) {

        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitud =
            OneTimeWorkRequestBuilder<ProspectoSyncWorker>()
                .setConstraints(restricciones)
                .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(
                TRABAJO_SYNC,
                ExistingWorkPolicy.KEEP,
                solicitud
            )
    }

    // Sincronización manual
    fun forzarSincronizacion(context: Context) {

        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitud =
            OneTimeWorkRequestBuilder<ProspectoSyncWorker>()
                .setConstraints(restricciones)
                .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(
                TRABAJO_SYNC,
                ExistingWorkPolicy.REPLACE,
                solicitud
            )
    }
}
