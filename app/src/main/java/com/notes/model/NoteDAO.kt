package com.notes.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDAO {

    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Delete
    fun delete(note: Note)

    @Query("DELETE FROM note_table")
    fun deleteAllNotes()

    @Query("Select * from note_table order by priority desc, title")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("Select * from note_table where title like '%' || :key || '%' order by priority desc, title")
    fun getAllNotes(key:String): LiveData<List<Note>>
}