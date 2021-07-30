package com.sqcw.notenapp.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MetaInformation(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val name: String = "",
    val halbjahr: String = "12/1",
    val schnitt121: Float = 0f,
    val schnitt122: Float = 0f,
    val schnitt131: Float = 0f,
    val schnitt132: Float = 0f,
    val pruefungsSchnitt: Float = 0f,
    val abiSchnitt: Float = 0f
)
