package com.sqcw.notenapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sqcw.notenapp.db.entities.Fach
import com.sqcw.notenapp.db.entities.MetaInformation
import com.sqcw.notenapp.db.entities.Note

@Database(
    entities = [
        MetaInformation::class,
        Fach::class,
        Note::class
    ],
    version = 1
)
abstract class NotenAppDatabase : RoomDatabase() {
    abstract fun dao(): NotenAppDao

    companion object {
        @Volatile
        private var INSTANCE: NotenAppDatabase? = null

        fun getInstance(context: Context): NotenAppDatabase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        NotenAppDatabase::class.java, "app_database.db"
                    )
                        .build()
                }

                return INSTANCE!!
            }
        }

    }
}