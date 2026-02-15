package my.noveldokusha.scraper

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.network.NetworkClient
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalSourceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val networkClient: NetworkClient
) {
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)
    val sources = MutableStateFlow<List<SourceInterface.Catalog>>(emptyList())

    init {
        scope.launch {
            appPreferences.SOURCES_EXTERNAL_DIRECTORY_URI.flow().collect {
                reload()
            }
        }
    }

    fun reload() {
        val uriString = appPreferences.SOURCES_EXTERNAL_DIRECTORY_URI.value
        if (uriString.isEmpty()) {
            sources.value = emptyList()
            return
        }

        try {
            val directoryUri = Uri.parse(uriString)
            val directory = DocumentFile.fromTreeUri(context, directoryUri)
            if (directory == null || !directory.canRead()) return

            val dynamicSources = directory.listFiles()
                .filter { it.name?.endsWith(".json") == true }
                .mapNotNull { file ->
                    try {
                        context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                            val config = gson.fromJson(InputStreamReader(inputStream), DynamicSourceConfig::class.java)
                            DynamicSource(
                                id = config.id,
                                name = config.name,
                                baseUrl = config.baseUrl,
                                catalogUrl = config.catalogUrl,
                                selectors = config.selectors,
                                language = LanguageCode.values().find { it.iso639_1 == config.language },
                                networkClient = networkClient
                            )
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            sources.value = dynamicSources
        } catch (e: Exception) {
            // Log error
        }
    }
}

data class DynamicSourceConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val catalogUrl: String,
    val language: String,
    val selectors: Map<String, String>
)
