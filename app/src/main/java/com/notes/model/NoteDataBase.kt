package com.notes.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Note::class], version = 2, exportSchema = false)
abstract class NoteDataBase : RoomDatabase() {

    companion object {

        var instance: NoteDataBase? = null

        fun getInstance(context: Context): NoteDataBase? {

            return instance ?: ({
                synchronized(NoteDataBase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NoteDataBase::class.java,
                        "note_db"
                    )
                        .fallbackToDestructiveMigration()
                        .addCallback(roomCallBack)
                        .build()
                }
                instance
            })()
        }

        object roomCallBack : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DoAsync {
                    val noteDao = instance?.noteDao()
                    noteDao?.insert(
                        Note(
                            title = "first title",
                            description = "First note just trying",
                            priority = 1,
                            pinToTop = false
                        )
                    )
                    noteDao?.insert(
                        Note(
                            title = "second title",
                            description = "second note just trying",
                            priority = 2,
                            pinToTop = false
                        )
                    )
                    noteDao?.insert(
                        Note(
                            title = "third title",
                            description = "third note just trying",
                            priority = 3,
                            pinToTop = false
                        )
                    )
                }
            }
        }

    }

    abstract fun noteDao(): NoteDAO
}
