package my.noveldokusha.scraper

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.toJson
import java.io.File
import java.io.FileReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceRepositoryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val networkClient: NetworkClient
) {
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val sourcesFolder = File(context.filesDir, "dynamic_sources").apply { mkdirs() }
    
    val installedSources = MutableStateFlow<List<SourceInterface.Catalog>>(emptyList())

    init {
        loadLocalSources()
        // Listen for repo URL changes to potentially auto-update (optional)
    }

    private fun loadLocalSources() {
        val files = sourcesFolder.listFiles { file -> file.extension == "json" } ?: return
        val list = files.mapNotNull { file ->
            try {
                val config = gson.fromJson(FileReader(file), DynamicSourceConfig::class.java)
                createDynamicSource(config)
            } catch (e: Exception) {
                null
            }
        }
        installedSources.value = list
    }

    suspend fun fetchRepositoryIndex(url: String): RepositoryIndex? {
        return try {
            val json = networkClient.get(url).toJson()
            gson.fromJson(json, RepositoryIndex::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun installSource(sourceInfo: RepositorySourceInfo) {
        try {
            val json = networkClient.get(sourceInfo.jsonUrl).toJson().toString()
            val file = File(sourcesFolder, "${sourceInfo.id}.json")
            file.writeText(json)
            loadLocalSources()
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun deleteSource(id: String) {
        File(sourcesFolder, "$id.json").delete()
        loadLocalSources()
    }

    private fun createDynamicSource(config: DynamicSourceConfig): DynamicSource {
        return DynamicSource(
            id = config.id,
            name = config.name,
            baseUrl = config.baseUrl,
            catalogUrl = config.catalogUrl,
            selectors = config.selectors,
            language = LanguageCode.values().find { it.iso639_1 == config.language },
            networkClient = networkClient
        )
    }
}

data class RepositoryIndex(
    val name: String,
    val sources: List<RepositorySourceInfo>
)

data class RepositorySourceInfo(
    val id: String,
    val name: String,
    val version: Int,
    val baseUrl: String,
    val jsonUrl: String
)
