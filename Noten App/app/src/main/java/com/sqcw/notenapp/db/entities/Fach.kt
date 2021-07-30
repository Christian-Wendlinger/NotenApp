package com.sqcw.notenapp.db.entities

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Fach(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String = "",
    var farbe: Int = Color.BLACK,
    val fachart: String = "Sonstige",
    var gewicht: Int = 1,
    val halbjahr: String,
    val klausurenSchnitt: Float = 0f,
    val sonstigeSchnitt: Float = 0f,
    val schnitt: Float = 0f,
    val endnote: Int = 0,
    val beinhaltetNoten: Boolean = false
)
