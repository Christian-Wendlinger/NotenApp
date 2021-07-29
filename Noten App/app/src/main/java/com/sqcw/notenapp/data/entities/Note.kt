package com.sqcw.notenapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val datum: String,
    val punktzahl: Int,
    val art: String,
    val bemerkung: String,
    val fachId: Int
)
