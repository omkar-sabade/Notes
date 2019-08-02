package com.notes.model

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData


class DoAsync(val function: () -> Unit) : AsyncTask<Any, Unit, Unit>() {
    init {
        execute()
    }

    override fun doInBackground(vararg params: Any?) {
        function()
    }

}

class NoteRepository(application: Application) {
    private var noteDAO: NoteDAO
    private var liveData: LiveData<List<Note>>

    init {
        val noteDataBase: NoteDataBase = NoteDataBase.getInstance(application.applicationContext)!!
        noteDAO = noteDataBase.noteDao()
        liveData = noteDAO.getAllNotes()
    }

    fun insert(note: Note) {
        DoAsync {
            noteDAO.insert(note)
        }
    }

    fun delete(note: Note) {
        DoAsync {
            noteDAO.delete(note)
        }
    }

    fun update(note: Note) {
        DoAsync {
            noteDAO.update(note)
        }
    }

    fun getAllNotes(): LiveData<List<Note>> {
        return noteDAO.getAllNotes()
    }

    fun getAllNotes(key:String): LiveData<List<Note>> {
        return noteDAO.getAllNotes(key)
    }

    fun deleteAllNotes() {
        DoAsync {
            noteDAO.deleteAllNotes()
        }
    }
}