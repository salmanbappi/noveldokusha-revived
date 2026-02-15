package my.noveldokusha.feature.local_database.tables

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [Index(value = ["chapterUrl", "itemPosition"], unique = true)]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookUrl: String,
    val chapterUrl: String,
    val chapterTitle: String,
    val itemPosition: Int,
    val textSnippet: String,
    val createdAtEpochMilli: Long = System.currentTimeMillis()
)
