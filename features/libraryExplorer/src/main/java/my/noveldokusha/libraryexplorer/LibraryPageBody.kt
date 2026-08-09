package my.noveldokusha.libraryexplorer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import my.noveldoksuha.coreui.components.BookImageButtonView
import my.noveldoksuha.coreui.components.ImageViewGlide
import my.noveldoksuha.coreui.modifiers.bounceOnPressed
import my.noveldoksuha.coreui.theme.ColorAccent
import my.noveldoksuha.coreui.theme.ImageBorderShape
import my.noveldokusha.core.appPreferences.ListLayoutMode
import my.noveldokusha.core.isLocalUri
import my.noveldokusha.core.rememberResolvedBookImagePath
import my.noveldokusha.feature.local_database.BookWithContext

@Composable
internal fun LibraryPageBody(
    list: List<BookWithContext>,
    layoutMode: ListLayoutMode,
    onClick: (BookWithContext) -> Unit,
    onLongClick: (BookWithContext) -> Unit,
) {
    if (list.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.AutoStories,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFA0A0A0)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.library_is_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else if (layoutMode == ListLayoutMode.VerticalGrid) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp, start = 4.dp, end = 4.dp)
        ) {
            items(
                items = list,
                key = { it.book.url }
            ) {
                LibraryGridItem(it, onClick, onLongClick)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
        ) {
            items(
                items = list,
                key = { it.book.url }
            ) {
                LibraryListItem(it, onClick, onLongClick)
            }
        }
    }
}

@Composable
private fun LibraryGridItem(
    it: BookWithContext,
    onClick: (BookWithContext) -> Unit,
    onLongClick: (BookWithContext) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box {
        BookImageButtonView(
            title = it.book.title,
            coverImageModel = rememberResolvedBookImagePath(
                bookUrl = it.book.url,
                imagePath = it.book.coverImageUrl
            ),
            onClick = { onClick(it) },
            onLongClick = { onLongClick(it) },
            interactionSource = interactionSource,
            modifier = Modifier.bounceOnPressed(interactionSource)
        )
        
        val notReadCount = it.chaptersCount - it.chaptersReadCount
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (notReadCount != 0) {
                Badge(
                    text = notReadCount.toString(),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            }
            if (it.book.completed) {
                Badge(
                    text = stringResource(R.string.completed),
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color.White
                )
            }
        }

        if (it.book.url.isLocalUri) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                Badge(
                    text = stringResource(R.string.local),
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun LibraryListItem(
    it: BookWithContext,
    onClick: (BookWithContext) -> Unit,
    onLongClick: (BookWithContext) -> Unit,
) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF121212),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262626))
    ) {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = { onClick(it) },
                onLongClick = { onLongClick(it) }
            ),
            colors = androidx.compose.material3.ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            leadingContent = {
                ImageViewGlide(
                    imageModel = rememberResolvedBookImagePath(
                        bookUrl = it.book.url,
                        imagePath = it.book.coverImageUrl
                    ),
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    error = my.noveldoksuha.coreui.R.drawable.default_icon
                )
            },
            headlineContent = {
                Text(
                    text = it.book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Column {
                    val domain = try {
                        val uri = android.net.Uri.parse(it.book.url)
                        uri.host ?: it.book.url
                    } catch (e: Exception) {
                        it.book.url
                    }
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA0A0A0)
                    )
                    Text(
                        text = "${it.chaptersReadCount} / ${it.chaptersCount} chapters",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE0E0E0),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            trailingContent = {
                val notReadCount = it.chaptersCount - it.chaptersReadCount
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (notReadCount != 0) {
                        Badge(
                            text = notReadCount.toString(),
                            containerColor = Color(0xFF262626),
                            contentColor = Color.White
                        )
                    }
                    if (it.book.completed) {
                        Spacer(Modifier.size(4.dp))
                        Badge(
                            text = "Done",
                            containerColor = Color(0xFF1E1E1E),
                            contentColor = Color.White
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun Badge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
