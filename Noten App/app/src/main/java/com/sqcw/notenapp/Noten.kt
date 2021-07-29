package com.sqcw.notenapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.adapters.FachListeAdapter
import com.sqcw.notenapp.data.TheViewModel
import com.sqcw.notenapp.data.entities.Fach
import com.sqcw.notenapp.data.entities.MetaInformation
import com.sqcw.notenapp.util.LinearSpacingManager


class Noten : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noten)

        // initialize Database, Check User Data and set Header Data
        val db = ViewModelProvider(this).get(TheViewModel::class.java)
        initializeUserData(db)
        setHeaderData(db)

        // initialize Fächerliste
        findViewById<RecyclerView>(R.id.notenFachListe).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FachListeAdapter(context, db)
            addItemDecoration(LinearSpacingManager(-50))

            // read Data from database
            db.readMetaInformation()
                .observe(context as LifecycleOwner, Observer { metaInformationList ->
                    if (metaInformationList.isEmpty()) return@Observer

                    val metaInformation = metaInformationList[0]

                    db.getHalbjahrMitFaecher(metaInformation.halbjahr)
                        .observe(context as LifecycleOwner, Observer { data ->
                            (adapter as FachListeAdapter).setData(data[0].faecher)
                        })
                })


        }
    }

    // read userInformation and create new user if necessary
    private fun initializeUserData(db: TheViewModel) {
        db.readMetaInformation().observe(this, Observer { data ->
            if (data.isNotEmpty()) return@Observer

            // if no user was created before
            db.createMetaInformation(
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
            )

            val testFaecher = listOf(
                Fach(0, "Deutsch", "#ad0e00", "Sonstige", 1, "12/1", 0f, 0f, 0f, 0, false),
                Fach(0, "Mathe", "#00277a", "Sonstige", 1, "12/1", 0f, 0f, 0f, 0, false),
                Fach(0, "Englisch", "#c7c702", "Sonstige", 1, "12/1", 0f, 0f, 0f, 0, false)
            )
            testFaecher.forEach { db.insertFach(it) }
        })
    }

    // set Header data
    fun setHeaderData(db: TheViewModel) {
        db.readMetaInformation().observe(this, Observer { metaInformationList ->
            if (metaInformationList.isEmpty()) return@Observer

            val metaInformation = metaInformationList[0]

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
        })
    }
}