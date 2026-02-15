package my.noveldokusha.feature.local_database.DAOs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import my.noveldokusha.feature.local_database.tables.Bookmark

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: Bookmark)

    @Delete
    suspend fun delete(bookmark: Bookmark)

    @Query("DELETE FROM Bookmark WHERE chapterUrl = :chapterUrl AND itemPosition = :itemPosition")
    suspend fun delete(chapterUrl: String, itemPosition: Int)

    @Query("SELECT * FROM Bookmark WHERE bookUrl = :bookUrl ORDER BY createdAtEpochMilli DESC")
    fun getBookmarksForBook(bookUrl: String): Flow<List<Bookmark>>

    @Query("SELECT * FROM Bookmark WHERE chapterUrl = :chapterUrl AND itemPosition = :itemPosition LIMIT 1")
    suspend fun getBookmark(chapterUrl: String, itemPosition: Int): Bookmark?

    @Query("SELECT EXISTS(SELECT 1 FROM Bookmark WHERE chapterUrl = :chapterUrl AND itemPosition = :itemPosition)")
    fun hasBookmark(chapterUrl: String, itemPosition: Int): Flow<Boolean>
}
