package com.sqcw.notenapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.adapters.FachAdapter
import com.sqcw.notenapp.db.NotenAppDao
import com.sqcw.notenapp.db.NotenAppDatabase
import com.sqcw.notenapp.db.entities.Fach
import com.sqcw.notenapp.db.entities.MetaInformation
import com.sqcw.notenapp.db.entities.Note
import com.sqcw.notenapp.util.LinearSpacingManager
import kotlinx.coroutines.launch


class Noten : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noten)

        // initialize Database, Check User Data and set Header Data
        val db = NotenAppDatabase.getInstance(this).dao()
        lifecycleScope.launch {
            init(db)
        }
    }

    // initialize Liste
    private suspend fun init(db: NotenAppDao) {
        // create new user data or read data
        val metaInformationList = db.readMetaInformation()
        val metaInformation =
            if (metaInformationList.isEmpty()) initializeUserData(db)
            else metaInformationList[0]

        setHeaderData(metaInformation)

        // read Fächer
        val faecherDbListe = db.getHalbjahrMitFaecher(metaInformation.halbjahr)
        val faecher: List<Fach> =
            if (faecherDbListe.isEmpty()) listOf()
            else faecherDbListe[0].faecher
        // this should read all Noten for every Fach
        val notenSammlung: MutableList<List<Note>> = mutableListOf()
        faecher.forEach { fach -> notenSammlung.add(db.getFachMitNoten(fach.id)[0].noten) }

        // initialize Recyclerview
        findViewById<RecyclerView>(R.id.notenFachListe).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FachAdapter(context, faecher, notenSammlung)

            // prevent multiple itemDecorations
            if (itemDecorationCount == 0) addItemDecoration(LinearSpacingManager(-50))
        }
    }


    // read userInformation and create new user if necessary
    private suspend fun initializeUserData(db: NotenAppDao): MetaInformation {
        val metaInformation = MetaInformation()
        db.createMetaInformation(metaInformation)

        // temporary
        val testFaecher = listOf(
            Fach(id = 1, name = "Deutsch", farbe = "#ad0e00", halbjahr = "12/1"),
            Fach(id = 2, name = "Mathe", farbe = "#00277a", halbjahr = "12/1"),
            Fach(id = 3, name = "Englisch", farbe = "#c7c702", halbjahr = "12/1")
        )
        testFaecher.forEach { db.insertFach(it) }

        return metaInformation
    }

    // set Header data
    private fun setHeaderData(metaInformation: MetaInformation) {
        //set Halbjahr
        findViewById<TextView>(R.id.notenHalbjahrText).apply {
            text = "Abschnitt: ${metaInformation.halbjahr}"
        }

        // set Halbjahrschnitt
        findViewById<TextView>(R.id.notenSchnittText).apply {
            text = when (metaInformation.halbjahr) {
                "12/1" -> "Schnitt: ${
                    if (metaInformation.schnitt121 == 0f) "N/A"
                    else "%.2f".format(metaInformation.schnitt121)
                }"
                "12/2" -> "Schnitt: ${
                    if (metaInformation.schnitt122 == 0f) "N/A"
                    else "%.2f".format(metaInformation.schnitt122)
                }"
                "13/1" -> "Schnitt: ${
                    if (metaInformation.schnitt131 == 0f) "N/A"
                    else "%.2f".format(metaInformation.schnitt131)
                }"
                "13/2" -> "Schnitt: ${
                    if (metaInformation.schnitt132 == 0f) "N/A"
                    else "%.2f".format(metaInformation.schnitt132)
                }"
                else -> "Prüfungsschnitt: ${
                    if (metaInformation.pruefungsSchnitt == 0f) "N/A"
                    else "%.2f".format(metaInformation.pruefungsSchnitt)
                }"
            }
        }

        // set Abinote
        findViewById<TextView>(R.id.notenAbischnittText).apply {
            text = "Abi-Note: ${
                if (metaInformation.abiSchnitt == 0f) "N/A"
                else "%.2f".format(metaInformation.abiSchnitt)
            }"
        }
    }

    // override Resume to change the data after finishing another activity
    override fun onResume() {
        super.onResume()

        // update the view!
        val db = NotenAppDatabase.getInstance(this).dao()
        lifecycleScope.launch {
            init(db)
        }
    }
}