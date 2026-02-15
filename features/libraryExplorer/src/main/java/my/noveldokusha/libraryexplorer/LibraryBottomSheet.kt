package my.noveldokusha.libraryexplorer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import my.noveldoksuha.coreui.components.PosNegCheckbox
import my.noveldokusha.core.appPreferences.LibrarySortMode
import my.noveldokusha.core.utils.toToggleableState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun LibraryBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    model: LibraryViewModel = viewModel()
) {
    if (!visible) return

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(top = 16.dp, bottom = 64.dp)) {
            Text(
                text = stringResource(id = R.string.filter),
                modifier = Modifier
                    .padding(8.dp)
                    .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            PosNegCheckbox(
                text = stringResource(id = R.string.read),
                state = model.readFilter.toToggleableState(),
                onStateChange = { model.readFilterToggle() },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(id = R.string.sort),
                modifier = Modifier
                    .padding(8.dp)
                    .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LibrarySortMode.entries.forEach { mode ->
                    FilterChip(
                        selected = model.sortMode == mode,
                        onClick = { model.updateSortMode(mode) },
                        label = { Text(mode.name.replace("([a-z])([A-Z])".toRegex(), "$1 $2")) }
                    )
                }
            }
        }
    }
}
