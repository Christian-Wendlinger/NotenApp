package com.sqcw.notenapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.adapters.FachListeAdapter
import com.sqcw.notenapp.data.TheDao
import com.sqcw.notenapp.data.TheDatabase
import com.sqcw.notenapp.data.entities.Fach
import com.sqcw.notenapp.data.entities.MetaInformation
import com.sqcw.notenapp.data.entities.Note
import com.sqcw.notenapp.util.LinearSpacingManager
import kotlinx.coroutines.launch


class Noten : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noten)

        // initialize Database, Check User Data and set Header Data
        val db = TheDatabase.getInstance(this).dao()
        lifecycleScope.launch {
            init(db)
        }
    }

    // initialize Liste
    private suspend fun init(db: TheDao) {
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
            adapter = FachListeAdapter(context, faecher, notenSammlung)

            // prevent multiple itemDecorations
            if (itemDecorationCount == 0) addItemDecoration(LinearSpacingManager(-50))
        }
    }


    // read userInformation and create new user if necessary
    private suspend fun initializeUserData(db: TheDao): MetaInformation {
        val metaInformation =
            MetaInformation(
                0,
                "",
                "12/1",
                0f,
                0f,
                0f,
                0f,
                0f,
                0f
            )

        db.createMetaInformation(metaInformation)

        // temporary
        val testFaecher = listOf(
            Fach(0, "Deutsch", "#ad0e00", "Sonstige", 1, "12/1", 0f, 0f, 0f, 0, false),
            Fach(0, "Mathe", "#00277a", "Sonstige", 1, "12/1", 0f, 0f, 0f, 0, false),
            Fach(0, "Englisch", "#c7c702", "Sonstige", 1, "12/1", 0f, 0f, 0f, 0, false)
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
        val db = TheDatabase.getInstance(this).dao()
        lifecycleScope.launch {
            init(db)
        }
    }
}