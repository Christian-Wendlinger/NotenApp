package com.sqcw.notenapp.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.sqcw.notenapp.data.TheViewModel
import com.sqcw.notenapp.data.entities.Note

fun updateFachInformation(
    viewModelStoreOwner: ViewModelStoreOwner,
    lifecycleOwner: LifecycleOwner,
    id: Int
) {
    val db = ViewModelProvider(viewModelStoreOwner).get(TheViewModel::class.java)

    // Noten auslesen
    db.getFachMitNoten(id).observe(lifecycleOwner, Observer { data ->
        if (data.isEmpty()) return@Observer

        val fach = data[0].fach
        val notenListe = data[0].noten

        // Klausurnoten werden 1 zu 1 mit den restlichen Noten verrechnet
        val klausuren = notenListe.filter { note -> note.art == "Klausur" }
        val sonstige = notenListe.filter { note -> note.art == "Normale Note" }

        var klausurenSchnitt = 0f
        var sonstigeSchnitt = 0f
        var gesamtSchnitt = 0f
        var gesamtNote = 0
        var beinhaltetNoten = true

        // calculate Schnitt
        if (klausuren.isEmpty() && sonstige.isEmpty()) {
            beinhaltetNoten = false
        } else if (klausuren.isNotEmpty() && sonstige.isEmpty()) {
            klausurenSchnitt = calculateSchnitt(klausuren)
            gesamtSchnitt = klausurenSchnitt
        } else if (klausuren.isEmpty() && sonstige.isNotEmpty()) {
            sonstigeSchnitt = calculateSchnitt(sonstige)
            gesamtSchnitt = sonstigeSchnitt
        } else {
            klausurenSchnitt = calculateSchnitt(klausuren)
            sonstigeSchnitt = calculateSchnitt(sonstige)
            gesamtSchnitt = (klausurenSchnitt + sonstigeSchnitt) / 2
        }

        // gesamtNote runden
        val noteAbgeschnitten = gesamtSchnitt.toInt()
        val kommaAnteilDerNote: Float = gesamtSchnitt % noteAbgeschnitten
        gesamtNote = if (kommaAnteilDerNote < 0.5) noteAbgeschnitten else noteAbgeschnitten + 1

        // update variables
        fach.beinhaltetNoten = beinhaltetNoten
        fach.klausurenSchnitt = klausurenSchnitt
        fach.sonstigeSchnitt = sonstigeSchnitt
        fach.schnitt = gesamtSchnitt
        fach.endnote = gesamtNote

        // update data in database
        db.updateFach(fach)
    })
}

// calculate Schnitt für Halbjahr
fun updateHalbjahrInformation(
    viewModelStoreOwner: ViewModelStoreOwner,
    lifecycleOwner: LifecycleOwner,
    halbjahr: String
) {
    val db = ViewModelProvider(viewModelStoreOwner).get(TheViewModel::class.java)

    // read all Fach
    db.getHalbjahrMitFaecher(halbjahr).observe(lifecycleOwner, Observer { data ->
        if (data.isEmpty()) return@Observer

        val metaInformation = data[0].metaInformation
        val faecher = data[0].faecher.filter { fach -> fach.beinhaltetNoten }

        val halbjahrSchnitt =
            if (faecher.isEmpty()) 0f
            else faecher.sumOf { fach -> fach.endnote }.toFloat() / faecher.size

        when (halbjahr) {
            "12/1" -> metaInformation.schnitt121 = halbjahrSchnitt
            "12/2" -> metaInformation.schnitt122 = halbjahrSchnitt
            "13/1" -> metaInformation.schnitt131 = halbjahrSchnitt
            "13/2" -> metaInformation.schnitt132 = halbjahrSchnitt
            else -> metaInformation.pruefungsSchnitt = halbjahrSchnitt
        }

        // update MetaInformation
        db.updateMetaInformation(metaInformation)
    })
}

// calculate schnitt for Notenliste
private fun calculateSchnitt(list: List<Note>): Float {
    return list.map { note -> note.punktzahl * note.gewicht }.sum() /
            list.map { note -> note.gewicht }.sum()
}

