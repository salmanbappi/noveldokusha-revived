package my.noveldokusha.feature.local_database.tables

import android.os.Parcelable
import androidx.room.Index
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    indices = [
        Index(value = ["inLibrary"]),
        Index(value = ["lastReadChapter"])
    ]
)
data class Book(
    val title: String,
    @PrimaryKey val url: String,
    val completed: Boolean = false,
    val lastReadChapter: String? = null,
    val inLibrary: Boolean = false,
    val coverImageUrl: String = "",
        val description: String = "",
        val lastReadEpochTimeMilli: Long = 0,
        val lastUpdateEpochTimeMilli: Long = 0,
    ) : Parcelable
    