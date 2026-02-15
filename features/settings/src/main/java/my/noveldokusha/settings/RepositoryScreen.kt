package my.noveldokusha.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryScreen(onBack: () -> Unit) {
    val viewModel: RepositoryViewModel = viewModel()
    var showAddDialog by remember { mutableStateOf(false) }
    var newRepoUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, "Back")
                    }
                },
                title = { Text("Source Repositories") },
// ...
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Repo")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            item {
                Text(
                    "Managed Repositories",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            items(viewModel.repoUrls.value.toList()) { url ->
                ListItem(
                    headlineContent = { Text(url) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.removeRepo(url) }) {
                            Icon(Icons.Default.Delete, "Remove")
                        }
                    }
                )
            }

            item {
                Text(
                    "Available Sources",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            viewModel.availableRepos.forEach { repo ->
                item {
                    Text(
                        repo.name,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(repo.sources) { source ->
                    val isInstalled = viewModel.installedSources.value.any { it.id == source.id }
                    ListItem(
                        headlineContent = { Text(source.name) },
                        supportingContent = { Text(source.baseUrl) },
                        trailingContent = {
                            if (isInstalled) {
                                TextButton(onClick = { viewModel.deleteSource(source.id) }) {
                                    Text("Uninstall", color = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                IconButton(onClick = { viewModel.installSource(source) }) {
                                    Icon(Icons.Default.Download, "Install")
                                }
                            }
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Repository") },
                text = {
                    TextField(
                        value = newRepoUrl,
                        onValueChange = { newRepoUrl = it },
                        placeholder = { Text("https://example.com/index.json") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addRepo(newRepoUrl)
                        newRepoUrl = ""
                        showAddDialog = false
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
