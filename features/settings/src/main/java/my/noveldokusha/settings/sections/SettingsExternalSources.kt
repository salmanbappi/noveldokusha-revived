package my.noveldokusha.settings.sections

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import my.noveldoksuha.coreui.theme.textPadding
import my.noveldokusha.settings.R

@Composable
internal fun SettingsExternalSources(
    currentUri: String,
    onUriSelected: (String) -> Unit,
    onManageRepositories: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // Persist permission
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            onUriSelected(it.toString())
        }
    }

    Column {
        Text(
            text = "Advanced Sources",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.textPadding(),
            color = MaterialTheme.colorScheme.primary
        )
        ListItem(
            headlineContent = {
                Text(text = "External Sources Folder")
            },
            supportingContent = {
                Text(
                    text = if (currentUri.isEmpty()) 
                        "Select a folder to load custom JSON sources (like Mihon extensions)" 
                    else 
                        Uri.parse(currentUri).path ?: currentUri
                )
            },
            leadingContent = {
                Icon(Icons.Outlined.Folder, null, tint = MaterialTheme.colorScheme.onSurface)
            },
            modifier = Modifier.clickable { launcher.launch(null) }
        )
        ListItem(
            headlineContent = {
                Text(text = "Manage Remote Repositories")
            },
            supportingContent = {
                Text(text = "Add and install sources from online repositories (Tachiyomi style)")
            },
            leadingContent = {
                Icon(Icons.Outlined.CloudDownload, null, tint = MaterialTheme.colorScheme.onSurface)
            },
            modifier = Modifier.clickable { onManageRepositories() }
        )
    }
}
