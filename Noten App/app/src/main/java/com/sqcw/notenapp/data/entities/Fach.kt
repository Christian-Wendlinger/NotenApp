package com.sqcw.notenapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Fach(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String,
    var farbe: String,
    var fachart: String,
    var gewicht: Int,
    var halbjahr: String,
    var klausurenSchnitt: Float,
    var sonstigeSchnitt: Float,
    var schnitt: Float,
    var endnote: Int,
    var beinhaltetNoten: Boolean
)
