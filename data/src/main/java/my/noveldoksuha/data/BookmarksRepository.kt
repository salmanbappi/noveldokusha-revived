package my.noveldoksuha.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import my.noveldokusha.feature.local_database.AppDatabase
import my.noveldokusha.feature.local_database.tables.Bookmark
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarksRepository @Inject constructor(
    private val db: AppDatabase
) {
    fun getBookmarksForBook(bookUrl: String): Flow<List<Bookmark>> {
        return db.bookmarkDao().getBookmarksForBook(bookUrl)
    }

    suspend fun toggleBookmark(
        bookUrl: String,
        chapterUrl: String,
        chapterTitle: String,
        itemPosition: Int,
        textSnippet: String
    ): Boolean = withContext(Dispatchers.IO) {
        val existing = db.bookmarkDao().getBookmark(chapterUrl, itemPosition)
        if (existing != null) {
            db.bookmarkDao().delete(existing)
            false
        } else {
            db.bookmarkDao().insert(
                Bookmark(
                    bookUrl = bookUrl,
                    chapterUrl = chapterUrl,
                    chapterTitle = chapterTitle,
                    itemPosition = itemPosition,
                    textSnippet = textSnippet.take(200) // Keep it short
                )
            )
            true
        }
    }

    fun hasBookmark(chapterUrl: String, itemPosition: Int): Flow<Boolean> {
        return db.bookmarkDao().hasBookmark(chapterUrl, itemPosition)
    }
}
