package com.sqcw.notenapp.util

import android.content.Context
import com.sqcw.notenapp.db.NotenAppDatabase
import com.sqcw.notenapp.db.entities.Fach
import com.sqcw.notenapp.db.entities.Note
import kotlin.math.round

// update database after change
suspend fun updateDb(
    context: Context,
    fach: Fach,
    halbjahr: String
) {
    updateFachInDb(context, fach)
    updateHalbjahrInDb(context, halbjahr)
    calculateAbiturnote(context)
}

// calculate Schnitte and Endnote for a given Fach
private suspend fun updateFachInDb(
    context: Context,
    fach: Fach
) {
    // special case to prevent updating
    if (fach.id == -1) return

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


    // update data in database with new values
    db.updateFach(
        fach.copy(
            beinhaltetNoten = beinhaltetNoten,
            klausurenSchnitt = klausurenSchnitt,
            sonstigeSchnitt = sonstigeSchnitt,
            gesamtSchnitt = gesamtSchnitt,
            endnote = round(gesamtSchnitt).toInt()
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
            else -> metaInformation.copy(pruefungsSchnitt = halbjahrSchnitt)
        }
    )
}

/*
Berechnung:
Profilfach 4 Kurse *2
Pflichtfächer Kurse
Restliche Fächer (bis 36 Kurse)
Abiturnoten 5 Stück *4

Gesamtpunktzahl mit Tabelle abgleichen

Fehlende Noten werden durch die Durchschnittsnote ergänzt
 */
private suspend fun calculateAbiturnote(context: Context) {
    val db = NotenAppDatabase.getInstance(context).dao()

    // read all Fach which already has Noten
    var profilFachNoten =
        db.getProfilKurse().filter { it.beinhaltetNoten }.map { it.endnote } as MutableList
    var pflichtFachNoten =
        db.getPflichtKurse().filter { it.beinhaltetNoten }.map { it.endnote } as MutableList
    var restlicheNoten =
        db.getRestlicheKurse().filter { it.beinhaltetNoten }.map { it.endnote } as MutableList
    var abiturNoten =
        db.getAbiturNoten().filter { it.beinhaltetNoten }.map { it.endnote } as MutableList
    val metaInformation = db.readMetaInformation()[0]

    // No grades at all to calculate
    if (profilFachNoten.isEmpty() && pflichtFachNoten.isEmpty() && restlicheNoten.isEmpty() && abiturNoten.isEmpty()) {
        if (metaInformation.abiSchnitt != 0f) db.updateMetaInformation(
            metaInformation.copy(
                abiSchnitt = 0f
            )
        )
        return
    }

    // Gesamtschnitt (alle Halbjahre)
    val teilSchnitte = mutableListOf(
        metaInformation.schnitt121,
        metaInformation.schnitt122,
        metaInformation.schnitt131,
        metaInformation.schnitt132,
        metaInformation.pruefungsSchnitt
    ).filter { it != 0f }

    // not 0 exception since there must be at least 1 grade
    val durchSchnittsNote = round(teilSchnitte.sum() / teilSchnitte.size).toInt()


    // fill missing profil noten
    while (profilFachNoten.size < 4) {
        profilFachNoten.add(durchSchnittsNote)
    }

    // fill missing abitur noten
    while (abiturNoten.size < 5) {
        abiturNoten.add(durchSchnittsNote)
    }

    // too few grades
    var notenCount =
        restlicheNoten.size + pflichtFachNoten.size + profilFachNoten.size
    while (notenCount < 36) {
        restlicheNoten.add(durchSchnittsNote)
        notenCount++
    }

    // already more grades than necessary -> Filter for the best
    if (notenCount > 36) {
        val countToAchieve = 36 - (profilFachNoten.size + pflichtFachNoten.size)

        // BIG ERROR FROM USER !!! TOO MANY FÄCHER MARKED ALS PLFICHT OR PROFIL -> CALCULATION NOT POSSIBLE
        if (countToAchieve < 0) {
            return
        }

        // sort restliche Noten from lowest to highest and remove the first elements
        restlicheNoten.sort()

        while (restlicheNoten.size > countToAchieve) {
            restlicheNoten.removeAt(0)
        }
    }

    // At this point we should have exactly 36 Grades

    // sum of all
    val kursGesamtPunktzahl =
        profilFachNoten.sum() * 2 + pflichtFachNoten.sum() + restlicheNoten.sum()
    val abiturGesamtPunktzahl = abiturNoten.map { it * 4 }.sum()

    // check table and update database
    val gesamtPunkte = kursGesamtPunktzahl + abiturGesamtPunktzahl

    // update value in db
    db.updateMetaInformation(
        metaInformation.copy(abiSchnitt = getAbiturNoteFromPunktzahl(gesamtPunkte))
    )
}

// calculate schnitt for Notenliste
private fun calculateFachSchnitt(list: List<Note>): Float {
    return list.map { note -> note.punktzahl * note.gewicht }.sum() /
            list.map { note -> note.gewicht }.sum()
}

// every fach
private fun calculateHalbjahrSchnitt(list: List<Fach>): Float {
    return list.sumOf { it.endnote }.toFloat() / list.size
}


// table from wikipedia
private fun getAbiturNoteFromPunktzahl(punkte: Int): Float {
    when (punkte) {
        in 823..900 -> return 1.0f
        in 805..822 -> return 1.1f
        in 787..804 -> return 1.2f
        in 769..786 -> return 1.3f
        in 751..768 -> return 1.4f
        in 733..750 -> return 1.5f
        in 715..732 -> return 1.6f
        in 697..714 -> return 1.7f
        in 679..696 -> return 1.8f
        in 661..678 -> return 1.9f
        in 643..660 -> return 2.0f
        in 625..642 -> return 2.1f
        in 607..624 -> return 2.2f
        in 589..606 -> return 2.3f
        in 571..588 -> return 2.4f
        in 553..570 -> return 2.5f
        in 535..552 -> return 2.6f
        in 517..534 -> return 2.7f
        in 499..516 -> return 2.8f
        in 481..498 -> return 2.9f
        in 463..480 -> return 3.0f
        in 445..462 -> return 3.1f
        in 427..444 -> return 3.2f
        in 409..426 -> return 3.3f
        in 391..408 -> return 3.4f
        in 373..390 -> return 3.5f
        in 355..372 -> return 3.6f
        in 337..354 -> return 3.7f
        in 319..336 -> return 3.8f
        in 301..318 -> return 3.9f
        300 -> return 4.0f
        else -> return 6.0f
    }
}

