package com.sqcw.notenapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MetaInformation(
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    var name: String,
    var halbjahr: String,
    var schnitt121: Float,
    var schnitt122: Float,
    var schnitt131: Float,
    var schnitt132: Float,
    var pruefungsSchnitt: Float,
    var abiSchnitt: Float
)
