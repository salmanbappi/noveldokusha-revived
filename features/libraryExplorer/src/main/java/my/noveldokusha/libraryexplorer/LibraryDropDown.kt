package my.noveldokusha.libraryexplorer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import my.noveldokusha.core.appPreferences.ListLayoutMode
import my.noveldokusha.tooling.epub_importer.onDoImportEPUB

@Composable
internal fun LibraryDropDown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    layoutMode: ListLayoutMode,
    onLayoutModeChange: (ListLayoutMode) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Filled.FileOpen, stringResource(id = R.string.import_epub))
            },
            text = { Text(stringResource(id = R.string.import_epub)) },
            onClick = onDoImportEPUB()
        )
        
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    if (layoutMode == ListLayoutMode.VerticalGrid) Icons.Filled.ViewList else Icons.Filled.GridView,
                    null
                )
            },
            text = { 
                Text(
                    if (layoutMode == ListLayoutMode.VerticalGrid) stringResource(R.string.list) else stringResource(R.string.grid)
                ) 
            },
            onClick = {
                onLayoutModeChange(
                    if (layoutMode == ListLayoutMode.VerticalGrid) ListLayoutMode.VerticalList else ListLayoutMode.VerticalGrid
                )
                onDismiss()
            }
        )
    }
}