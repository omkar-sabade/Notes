package com.notes.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "note_table")
class Note(
    var title: String,
    var description: String,
    var priority: Int,
    var pinToTop: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    override fun equals(other: Any?): Boolean = when (other) {
        is Note -> other.title == this.title && other.description == this.description && other.priority == this.priority
        else -> super.equals(other)
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + priority
        result = 31 * result + pinToTop.hashCode()
        result = 31 * result + id
        return result
    }

}