package com.sqcw.notenapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.adapters.FachListeAdapter
import com.sqcw.notenapp.data.TheViewModel
import com.sqcw.notenapp.data.entities.Fach
import com.sqcw.notenapp.data.entities.MetaInformation
import com.sqcw.notenapp.data.entities.Note
import com.sqcw.notenapp.util.LinearSpacingManager


class Noten : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noten)

        // initialize Database and UserData
        val db = ViewModelProvider(this).get(TheViewModel::class.java)
        initializeUserData(db)

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
                    0f
                )
            )

            val testFaecher = listOf(
                Fach(0, "Deutsch", "#ad0e00", "Sonstige", 1, "12/1", 0f, 0, false),
                Fach(0, "Mathe", "#00277a", "Sonstige", 1, "12/1", 0f, 0, false),
                Fach(0, "Englisch", "#c7c702", "Sonstige", 1, "12/1", 0f, 0, false)
            )
            testFaecher.forEach { db.insertFach(it) }

            val testNoten = listOf(
                Note(0, "12.08.2021", 12, "Klausur", "", 1),
                Note(0, "12.08.2021", 10, "Klausur", "", 1),
                Note(0, "12.08.2021", 12, "Normale Note", "", 1),
                Note(0, "12.08.2021", 10, "Normale Note", "", 2),
                Note(0, "12.08.2021", 12, "Normale Note", "", 2),
            )
            testNoten.forEach { db.insertNote(it) }

        })
    }
}