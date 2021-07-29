package com.sqcw.notenapp.data

import androidx.lifecycle.LiveData
import com.sqcw.notenapp.data.entities.Fach
import com.sqcw.notenapp.data.entities.MetaInformation
import com.sqcw.notenapp.data.entities.Note
import com.sqcw.notenapp.data.relations.FachAndNoten
import com.sqcw.notenapp.data.relations.MetaInformationAndFaecher

class TheRepository(private val dao: TheDao) {
    // MetaInformation
    suspend fun createMetaInformation(metaInformation: MetaInformation) =
        dao.createMetaInformation(metaInformation)

    suspend fun updateMetaInformation(metaInformation: MetaInformation) =
        dao.updateMetaInformation(metaInformation)

    fun readMetaInformation(): LiveData<List<MetaInformation>> = dao.readMetaInformation()


    // Fach
    suspend fun insertFach(fach: Fach) = dao.insertFach(fach)
    suspend fun updateFach(fach: Fach) = dao.updateFach(fach)
    suspend fun deleteFach(fach: Fach) = dao.deleteFach(fach)
    fun readFach(id: Int): LiveData<List<Fach>> = dao.readFach(id)


    // Note
    suspend fun insertNote(note: Note) = dao.insertNote(note)
    suspend fun updateNote(note: Note) = dao.updateNote(note)
    suspend fun deleteNote(note: Note) = dao.deleteNote(note)

    // read specific Note
    fun readNote(id: Int): LiveData<List<Note>> = dao.readNote(id)


    // Relationen
    // Fach und Noten auslesen
    fun getFachMitNoten(fachId: Int): LiveData<List<FachAndNoten>> = dao.getFachMitNoten(fachId)

    // Alle Fächer für ein Halbjahr auslesen
    fun getHalbjahrMitFaecher(halbjahr: String): LiveData<List<MetaInformationAndFaecher>> =
        dao.getHalbjahrMitFaecher(halbjahr)
}