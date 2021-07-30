package com.sqcw.notenapp.util

import android.content.Context
import com.sqcw.notenapp.db.NotenAppDatabase
import com.sqcw.notenapp.db.entities.Fach
import com.sqcw.notenapp.db.entities.Note

// update database after change
suspend fun updateDb(
    context: Context,
    fach: Fach,
    halbjahr: String
) {
    updateFachInDb(context, fach)
    updateHalbjahrInDb(context, halbjahr)
}

// calculate Schnitte and Endnote for a given Fach
private suspend fun updateFachInDb(
    context: Context,
    fach: Fach
) {
    val db = NotenAppDatabase.getInstance(context).dao()
    // This should never crash!
    val notenListe = db.getFachMitNoten(fach.id)[0].noten

    // Klausurnoten werden 1 zu 1 mit den restlichen Noten verrechnet
    val klausuren = notenListe.filter { note -> note.art == "Klausur" }
    val sonstige = notenListe.filter { note -> note.art == "Normale Note" }

    // alle Schnitte die gebraucht werden
    var klausurenSchnitt = 0f
    var sonstigeSchnitt = 0f
    var gesamtSchnitt = 0f
    var beinhaltetNoten = true

    // calculate Schnitt
    if (klausuren.isEmpty() && sonstige.isEmpty()) {
        beinhaltetNoten = false
    } else if (klausuren.isNotEmpty() && sonstige.isEmpty()) {
        klausurenSchnitt = calculateFachSchnitt(klausuren)
        gesamtSchnitt = klausurenSchnitt
    } else if (klausuren.isEmpty() && sonstige.isNotEmpty()) {
        sonstigeSchnitt = calculateFachSchnitt(sonstige)
        gesamtSchnitt = sonstigeSchnitt
    } else {
        klausurenSchnitt = calculateFachSchnitt(klausuren)
        sonstigeSchnitt = calculateFachSchnitt(sonstige)
        gesamtSchnitt = (klausurenSchnitt + sonstigeSchnitt) / 2
    }

    // gesamtNote runden
    val noteAbgeschnitten = gesamtSchnitt.toInt()
    val kommaAnteilDerNote: Float = gesamtSchnitt % noteAbgeschnitten
    val gesamtNote = if (kommaAnteilDerNote < 0.5) noteAbgeschnitten else noteAbgeschnitten + 1

    // update data in database with new values
    db.updateFach(
        fach.copy(
            beinhaltetNoten = beinhaltetNoten,
            klausurenSchnitt = klausurenSchnitt,
            sonstigeSchnitt = sonstigeSchnitt,
            schnitt = gesamtSchnitt,
            endnote = gesamtNote
        )
    )
}

// calculate Schnitt für Halbjahr
private suspend fun updateHalbjahrInDb(
    context: Context,
    halbjahr: String
) {
    val db = NotenAppDatabase.getInstance(context).dao()
    // This should never crash!
    val dbObject = db.getHalbjahrMitFaecher(halbjahr)[0]
    val metaInformation = dbObject.metaInformation
    val faecher = dbObject.faecher.filter { fach -> fach.beinhaltetNoten }

    // calculate Schnitt and set it to the correct variable
    val halbjahrSchnitt: Float =
        if (faecher.isEmpty()) 0f
        else calculateHalbjahrSchnitt(faecher)

    // update MetaInformation
    db.updateMetaInformation(
        when (halbjahr) {
            "12/1" -> metaInformation.copy(schnitt121 = halbjahrSchnitt)
            "12/2" -> metaInformation.copy(schnitt122 = halbjahrSchnitt)
            "13/1" -> metaInformation.copy(schnitt131 = halbjahrSchnitt)
            "13/2" -> metaInformation.copy(schnitt132 = halbjahrSchnitt)
            else -> metaInformation.copy(abiSchnitt = halbjahrSchnitt)
        }
    )
}

// calculate schnitt for Notenliste
private fun calculateFachSchnitt(list: List<Note>): Float {
    return list.map { note -> note.punktzahl * note.gewicht }.sum() /
            list.map { note -> note.gewicht }.sum()
}

// every fach
private fun calculateHalbjahrSchnitt(list: List<Fach>): Float {
    return list.map { fach -> fach.endnote * fach.gewicht }.sum().toFloat() /
            list.map { fach -> fach.gewicht }.sum()
}

