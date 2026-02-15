package my.noveldokusha.tooling.application_workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldoksuha.data.LibraryBooksRepository
import my.noveldokusha.core.AppFileResolver
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
internal class CacheCleanupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val libraryBooksRepository: LibraryBooksRepository,
    private val appFileResolver: AppFileResolver,
) : CoroutineWorker(context, workerParameters) {

    companion object {
        const val TAG = "CacheCleanup"

        fun createPeriodicRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiresDeviceIdle(true)
                .setRequiresBatteryNotLow(true)
                .build()

            return PeriodicWorkRequestBuilder<CacheCleanupWorker>(7, TimeUnit.DAYS)
                .addTag(TAG)
                .setConstraints(constraints)
                .build()
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val libraryBooks = libraryBooksRepository.getAllInLibrary().map { 
                appFileResolver.getLocalBookFolderName(it.url) 
            }.toSet()
            
            val folderBooks = appFileResolver.folderBooks
            if (!folderBooks.exists()) return@withContext Result.success()

            val currentTime = System.currentTimeMillis()
            val thirtyDaysInMillis = TimeUnit.DAYS.toMillis(30)

            folderBooks.listFiles()?.forEach { bookFolder ->
                if (bookFolder.isDirectory && bookFolder.name !in libraryBooks) {
                    val lastModified = bookFolder.lastModified()
                    if (currentTime - lastModified > thirtyDaysInMillis) {
                        Timber.d("CacheCleanupWorker: Deleting old cache folder: ${bookFolder.name}")
                        bookFolder.deleteRecursively()
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "CacheCleanupWorker: Failed to cleanup cache")
            Result.retry()
        }
    }
}