package my.noveldokusha.settings

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import my.noveldoksuha.coreui.BaseViewModel
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.scraper.RepositoryIndex
import my.noveldokusha.scraper.RepositorySourceInfo
import my.noveldokusha.scraper.SourceRepositoryManager
import javax.inject.Inject

@HiltViewModel
class RepositoryViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val repositoryManager: SourceRepositoryManager
) : BaseViewModel() {
    
    val repoUrls = appPreferences.SOURCES_REPOSITORIES_URLS.state(viewModelScope)
    val availableRepos = mutableStateListOf<RepositoryIndex>()
    val installedSources = repositoryManager.installedSources

    init {
        refreshAll()
    }

    fun addRepo(url: String) {
        if (url.isEmpty()) return
        appPreferences.SOURCES_REPOSITORIES_URLS.value = 
            appPreferences.SOURCES_REPOSITORIES_URLS.value + url
        refreshAll()
    }

    fun removeRepo(url: String) {
        appPreferences.SOURCES_REPOSITORIES_URLS.value = 
            appPreferences.SOURCES_REPOSITORIES_URLS.value - url
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            availableRepos.clear()
            repoUrls.value.forEach { url ->
                repositoryManager.fetchRepositoryIndex(url)?.let {
                    availableRepos.add(it)
                }
            }
        }
    }

    fun installSource(source: RepositorySourceInfo) {
        viewModelScope.launch {
            repositoryManager.installSource(source)
        }
    }

    fun deleteSource(id: String) {
        repositoryManager.deleteSource(id)
    }
}
