package com.sqcw.notenapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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
import com.sqcw.notenapp.db.relations.FachAndNoten
import com.sqcw.notenapp.util.LinearSpacingManager
import kotlinx.coroutines.launch


class Noten : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noten)

        // onClickListener for Settings
        findViewById<ImageView>(R.id.notenSettingsIcon).apply {
            setOnClickListener {
                val intent = Intent(context, Settings::class.java)
                startActivity(intent)
            }
        }

        // initialize Database, Check User Data and set Header Data
        val db = NotenAppDatabase.getInstance(this).dao()
        lifecycleScope.launch {
            init(db)
            populateListAndSetHeaderData(db)
        }
    }

    // initialize Liste
    private suspend fun init(db: NotenAppDao) {
        // initialize Recyclerview
        findViewById<RecyclerView>(R.id.notenFachListe).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FachAdapter()

            // prevent multiple itemDecorations
            if (itemDecorationCount == 0) addItemDecoration(LinearSpacingManager(-35))
        }

        // create new user data or read data
        if (db.readMetaInformation().isEmpty()) db.createMetaInformation(MetaInformation())
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
                else -> "Prüfungen: ${
                    if (metaInformation.pruefungsSchnitt == 0f) "N/A"
                    else "%.2f".format(metaInformation.pruefungsSchnitt)
                }"
            }
        }

        // set Abinote
        findViewById<TextView>(R.id.notenAbischnittText).apply {
            text = "Abi-Note: ${
                if (metaInformation.abiSchnitt == 0f) "N/A"
                else "%.1f".format(metaInformation.abiSchnitt)
            }"
        }

        // set Name
        findViewById<TextView>(R.id.notenUserNameText).apply {
            text = if (metaInformation.name == "") "Vorname Nachname" else metaInformation.name
        }
    }

    // fun to read faecher and noten from database and populate the recyclerview
    private suspend fun populateListAndSetHeaderData(db: NotenAppDao) {
        //read metaInformation and setData
        val rawMetaInformation = db.readMetaInformation()
        val metaInformation =
            if (rawMetaInformation.isEmpty()) MetaInformation() else rawMetaInformation[0]
        setHeaderData(metaInformation)

        // read Fächer
        val faecherDbListe = db.getHalbjahrMitFaecher(metaInformation.halbjahr)
        val faecher: List<Fach> =
            if (faecherDbListe.isEmpty()) listOf()
            else faecherDbListe[0].faecher

        // get Noten for Fach as well for listAdapter
        val faecherAndNoten: MutableList<FachAndNoten> = mutableListOf()
        faecher.forEach { fach -> faecherAndNoten.add(db.getFachMitNoten(fach.id)[0]) }

        // submit new Data
        findViewById<RecyclerView>(R.id.notenFachListe).apply {
            (adapter as FachAdapter).submitList(faecherAndNoten)
        }
    }

    // override Resume to change the data after finishing another activity
    override fun onResume() {
        super.onResume()

        // update the view!
        val db = NotenAppDatabase.getInstance(this).dao()
        lifecycleScope.launch {
            populateListAndSetHeaderData(db)
        }
    }
}