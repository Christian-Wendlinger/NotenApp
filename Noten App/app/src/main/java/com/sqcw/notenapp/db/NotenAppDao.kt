package com.sqcw.notenapp.db

import androidx.room.*
import com.sqcw.notenapp.db.entities.Fach
import com.sqcw.notenapp.db.entities.MetaInformation
import com.sqcw.notenapp.db.relations.MetaInformationAndFaecher

@Dao
interface NotenAppDao {
    // MetaInformation
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createMetaInformation(metaInformation: MetaInformation)

    @Update
    suspend fun updateMetaInformation(metaInformation: MetaInformation)

    @Transaction
    @Query("SELECT * FROM metainformation WHERE id = 0")
    suspend fun readMetaInformation(): List<MetaInformation>


    // Fach
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFach(fach: Fach)

    @Update
    suspend fun updateFach(fach: Fach)

    @Delete
    suspend fun deleteFach(fach: Fach)

    // read specific Fach
    @Transaction
    @Query("SELECT * FROM fach WHERE id = :id")
    suspend fun readFach(id: Int): List<Fach>


    // Note
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: com.sqcw.notenapp.db.entities.Note)

    @Update
    suspend fun updateNote(note: com.sqcw.notenapp.db.entities.Note)

    @Delete
    suspend fun deleteNote(note: com.sqcw.notenapp.db.entities.Note)

    // read specific Note
    @Transaction
    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun readNote(id: Int): List<com.sqcw.notenapp.db.entities.Note>


    // Relationen
    // Fach und Noten auslesen
    @Transaction
    @Query("SELECT * FROM fach WHERE id = :fachId")
    suspend fun getFachMitNoten(fachId: Int): List<com.sqcw.notenapp.db.relations.FachAndNoten>

    // Alle Fächer für ein Halbjahr auslesen
    @Transaction
    @Query("SELECT * FROM metainformation WHERE halbjahr = :halbjahr")
    suspend fun getHalbjahrMitFaecher(halbjahr: String): List<MetaInformationAndFaecher>
}