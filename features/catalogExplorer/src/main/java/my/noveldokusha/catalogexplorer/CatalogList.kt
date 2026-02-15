package my.noveldokusha.catalogexplorer

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import my.noveldoksuha.coreui.components.AnimatedTransition
import my.noveldoksuha.coreui.components.ImageViewGlide
import my.noveldoksuha.coreui.theme.InternalTheme
import my.noveldoksuha.coreui.theme.PreviewThemes
import my.noveldoksuha.data.CatalogItem
import my.noveldokusha.scraper.DatabaseInterface
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.fixtures.fixturesCatalogList
import my.noveldokusha.scraper.fixtures.fixturesDatabaseList

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun CatalogList(
    innerPadding: PaddingValues,
    databasesList: List<DatabaseInterface>,
    sourcesList: List<CatalogItem>,
    onDatabaseClick: (DatabaseInterface) -> Unit,
    onSourceClick: (SourceInterface.Catalog) -> Unit,
    onSourceSetPinned: (id: String, pinned: Boolean) -> Unit,
    onSourceSetDefault: (id: String, isDefault: Boolean) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 300.dp),
        modifier = Modifier.padding(paddingValues = innerPadding)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.database),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
            )
        }

        items(databasesList) {
            ListItem(
                modifier = Modifier
                    .clickable { onDatabaseClick(it) },
                headlineContent = {
                    Text(
                        text = stringResource(id = it.nameStrId),
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.english),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                leadingContent = {
                    ImageViewGlide(
                        imageModel = it.iconUrl,
                        modifier = Modifier.size(28.dp),
                        error = R.drawable.default_icon
                    )
                }
            )
        }

        item {
            Text(
                text = stringResource(id = R.string.sources),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
            )
        }

        items(
            items = sourcesList,
            key = { it.catalog.id }
        ) { item ->
            ListItem(
                modifier = Modifier
                    .clickable { onSourceClick(item.catalog) }
                    .animateItemPlacement(),
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = item.catalog.nameStrId),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        if (item.isDefault) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(start = 4.dp).size(14.dp)
                            )
                        }
                    }
                },
                supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val langResId = item.catalog.language?.nameResId
                        if (langResId != null) Text(
                            text = stringResource(id = langResId),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item.catalog.status.forEach { status ->
                                val color = when(status) {
                                    SourceInterface.Catalog.Status.FAST -> MaterialTheme.colorScheme.primary
                                    SourceInterface.Catalog.Status.STABLE -> Color(0xFF4CAF50)
                                    SourceInterface.Catalog.Status.FORMATTING -> MaterialTheme.colorScheme.secondary
                                    SourceInterface.Catalog.Status.EXPERIMENTAL -> MaterialTheme.colorScheme.error
                                }
                                Surface(
                                    color = color.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = status.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                leadingContent = {
                    val icon = item.catalog.iconUrl
                    if (icon is ImageVector) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                        )
                    } else {
                        ImageViewGlide(
                            imageModel = icon,
                            modifier = Modifier.size(28.dp),
                            error = R.drawable.default_icon
                        )
                    }
                },
                trailingContent = {
                    Row {
                        val catalog = item.catalog
                        if (catalog is SourceInterface.Configurable) {
                            var openConfig by rememberSaveable { mutableStateOf(false) }
                            IconButton(
                                onClick = { openConfig = !openConfig },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = stringResource(R.string.configuration),
                                )
                            }
                            if (openConfig) {
                                AlertDialog(
                                    onDismissRequest = { openConfig = false },
                                    confirmButton = {
                                        FilledTonalButton(onClick = { openConfig = !openConfig }) {
                                            Text(text = stringResource(R.string.close))
                                        }
                                    },
                                    text = { catalog.ScreenConfig() },
                                    icon = {
                                        Icon(
                                            Icons.Filled.Settings,
                                            stringResource(id = R.string.configuration),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = { onSourceClick(item.catalog) },
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Visibility,
                                contentDescription = "Preview Source",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        IconButton(
                            onClick = { onSourceSetDefault(item.catalog.id, !item.isDefault) },
                        ) {
                            Icon(
                                imageVector = if (item.isDefault) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Set Default",
                                tint = if (item.isDefault) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onSourceSetPinned(item.catalog.id, !item.pinned) },
                        ) {
                            AnimatedTransition(targetState = item.pinned) { pinned ->
                                Icon(
                                    imageVector = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = stringResource(R.string.pin_or_unpin_source),
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@PreviewThemes
@Composable
private fun PreviewView() {
    val catalogItemsList = fixturesCatalogList().mapIndexed { index, it ->
        CatalogItem(
            catalog = it,
            pinned = index % 2 == 0,
            isDefault = index == 1
        )
    }

    InternalTheme {
        CatalogList(
            innerPadding = PaddingValues(),
            databasesList = fixturesDatabaseList(),
            sourcesList = catalogItemsList,
            onDatabaseClick = {},
            onSourceClick = {},
            onSourceSetPinned = { _, _ -> },
            onSourceSetDefault = { _, _ -> },
        )
        }
    }
    