package com.sqcw.notenapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Fach(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val farbe: String,
    val fachart: String,
    val gewicht: Int,
    val halbjahr: String,
    val schnitt: Float,
    val endnote: Int,
    val beinhaltetNoten: Boolean
)
