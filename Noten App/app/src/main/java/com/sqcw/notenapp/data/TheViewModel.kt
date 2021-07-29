package com.sqcw.notenapp.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.sqcw.notenapp.data.entities.Fach
import com.sqcw.notenapp.data.entities.MetaInformation
import com.sqcw.notenapp.data.entities.Note
import com.sqcw.notenapp.data.relations.FachAndNoten
import com.sqcw.notenapp.data.relations.MetaInformationAndFaecher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TheViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TheRepository

    init {
        val dao = TheDatabase.getInstance(application).dao()
        repository = TheRepository(dao)
    }


    // MetaInformation
    fun createMetaInformation(metaInformation: MetaInformation) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createMetaInformation(metaInformation)
        }
    }

    fun updateMetaInformation(metaInformation: MetaInformation) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMetaInformation(metaInformation)
        }
    }

    fun readMetaInformation(): LiveData<List<MetaInformation>> = repository.readMetaInformation()


    // Fach
    fun insertFach(fach: Fach) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFach(fach)
        }
    }

    fun updateFach(fach: Fach) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFach(fach)
        }
    }

    fun deleteFach(fach: Fach) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFach(fach)
        }
    }

    fun readFach(id: Int): LiveData<List<Fach>> = repository.readFach(id)


    // Note
    fun insertNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun readNote(id: Int): LiveData<List<Note>> = repository.readNote(id)


    // Relationen
    fun getFachMitNoten(fachId: Int): LiveData<List<FachAndNoten>> =
        repository.getFachMitNoten(fachId)

    fun getHalbjahrMitFaecher(halbjahr: String): LiveData<List<MetaInformationAndFaecher>> =
        repository.getHalbjahrMitFaecher(halbjahr)
}

