package com.sqcw.notenapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var datum: String,
    var punktzahl: Int,
    var gewicht: Float,
    var art: String,
    var bemerkung: String,
    var fachId: Int
)
