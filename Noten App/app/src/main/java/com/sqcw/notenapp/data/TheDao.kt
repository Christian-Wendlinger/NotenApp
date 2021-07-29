package com.sqcw.notenapp.data

import androidx.room.*
import com.sqcw.notenapp.data.entities.Fach
import com.sqcw.notenapp.data.entities.MetaInformation
import com.sqcw.notenapp.data.entities.Note
import com.sqcw.notenapp.data.relations.FachAndNoten
import com.sqcw.notenapp.data.relations.MetaInformationAndFaecher

@Dao
interface TheDao {
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
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // read specific Note
    @Transaction
    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun readNote(id: Int): List<Note>


    // Relationen
    // Fach und Noten auslesen
    @Transaction
    @Query("SELECT * FROM fach WHERE id = :fachId")
    suspend fun getFachMitNoten(fachId: Int): List<FachAndNoten>

    // Alle Fächer für ein Halbjahr auslesen
    @Transaction
    @Query("SELECT * FROM metainformation WHERE halbjahr = :halbjahr")
    suspend fun getHalbjahrMitFaecher(halbjahr: String): List<MetaInformationAndFaecher>
}