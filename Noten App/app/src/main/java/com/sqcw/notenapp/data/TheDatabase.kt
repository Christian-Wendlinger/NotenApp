package com.sqcw.notenapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sqcw.notenapp.data.entities.Fach
import com.sqcw.notenapp.data.entities.MetaInformation
import com.sqcw.notenapp.data.entities.Note

@Database(
    entities = [
        MetaInformation::class,
        Fach::class,
        Note::class
    ],
    version = 1
)
abstract class TheDatabase : RoomDatabase() {
    abstract fun dao(): TheDao

    companion object {
        @Volatile
        private var INSTANCE: TheDatabase? = null

        fun getInstance(context: Context): TheDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TheDatabase::class.java,
                    "noten_app_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}