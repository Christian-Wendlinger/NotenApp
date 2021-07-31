package com.sqcw.notenapp.db.entities

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Fach(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String = "",
    var farbeHintergrund: Int = Color.parseColor("#006064"),
    var farbeText: Int = Color.parseColor("#eeeeee"),
    var profilFach: Boolean = false,
    var pflichtFach: Boolean = false,
    val halbjahr: String,
    val klausurenSchnitt: Float = 0f,
    val sonstigeSchnitt: Float = 0f,
    val gesamtSchnitt: Float = 0f,
    val endnote: Int = 0,
    val beinhaltetNoten: Boolean = false
)
