package com.sqcw.notenapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MetaInformation(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String,
    val halbjahr: String,
    val schnitt121: Float,
    val schnitt122: Float,
    val schnitt131: Float,
    val schnitt132: Float,
    val abiSchnitt: Float
)
