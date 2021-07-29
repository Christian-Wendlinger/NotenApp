package com.sqcw.notenapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.sqcw.notenapp.data.TheViewModel
import com.sqcw.notenapp.data.entities.Note
import com.sqcw.notenapp.util.updateFachInformation
import com.sqcw.notenapp.util.updateHalbjahrInformation
import java.text.SimpleDateFormat
import java.util.*

class AddNeueNote : AppCompatActivity() {

    private val notenArten = arrayOf("Normale Note", "Klausur")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neue_note)

        // initialize Database
        val db = ViewModelProvider(this).get(TheViewModel::class.java)

        // initialize Dropdown values
        findViewById<AutoCompleteTextView>(R.id.neueNoteArt).apply {
            keyListener = null
            setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    notenArten
                )
            )
            setText(notenArten[0], false)
        }

        // close Icon
        findViewById<ImageView>(R.id.neueNoteCloseIcon).apply {
            setOnClickListener { finish() }
        }

        // initialize values
        // title
        findViewById<TextView>(R.id.neueNoteTitel).apply {
            val id: Int = intent.extras!!.getInt("id")

            db.readFach(id).observe(context as LifecycleOwner, Observer { fachListe ->
                if (fachListe.isEmpty()) return@Observer

                val fach = fachListe[0]
                text = fach.name
            })
        }

        // set Gewicht to 1 as standard
        findViewById<EditText>(R.id.neueNoteGewicht).apply {
            setText("1")
        }

        // set Date to today
        findViewById<EditText>(R.id.neueNoteDatum).apply {
            val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
            setText(date)
        }

        // initialize Listener to add new Note
        findViewById<ImageView>(R.id.neueNoteAccept).apply {
            setOnClickListener {
                val note = parseInput()
                if (note != null) {
                    db.insertNote(note)

                    // how user that Note was inserted and update Fachinformation
                    db.readFach(intent.extras!!.getInt("id"))
                        .observe(context as LifecycleOwner, Observer { fachListe ->
                            if (fachListe.isEmpty()) return@Observer

                            val fach = fachListe[0]

                            // update Fach information and store new information to database
                            updateFachInformation(
                                context as ViewModelStoreOwner,
                                context as LifecycleOwner,
                                fach.id
                            )

                            // update Halbjahr Information
                            db.readMetaInformation()
                                .observe(context as LifecycleOwner, Observer { data ->
                                    if (data.isEmpty()) return@Observer

                                    val metaInformation = data[0]
                                    updateHalbjahrInformation(
                                        context as ViewModelStoreOwner,
                                        context as LifecycleOwner,
                                        metaInformation.halbjahr
                                    )
                                })
                        })

                    // notify user about update and close activity
                    rootView.findViewById<TextView>(R.id.neueNoteTitel).also {
                        Toast.makeText(
                            applicationContext,
                            "Neue Note für ${it.text} eingefügt!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                }
            }
        }
    }

    // check input
    private fun parseInput(): Note? {
        val date = findViewById<EditText>(R.id.neueNoteDatum).text.toString()
        val note = findViewById<EditText>(R.id.neueNoteNote).text.toString()
        val gewicht = findViewById<EditText>(R.id.neueNoteGewicht).text.toString()
        val art = findViewById<EditText>(R.id.neueNoteArt).text.toString()
        val bemerkung = findViewById<EditText>(R.id.neueNoteBemerkung).text.toString()

        // check date format
        if (!date.matches(Regex("^(0[1-9]|[12][0-9]|3[01])[.](0[1-9]|1[012])[.]20[0-9]{2}$"))
        ) {
            Toast.makeText(this, "Datum muss im Format mm.DD.yyyy sein!", Toast.LENGTH_SHORT).show()
            return null
        }

        // check note
        val checkedNote: Int? = note.toIntOrNull()
        if (checkedNote == null || checkedNote < 0 || checkedNote > 15) {
            Toast.makeText(this, "Note muss 0 — 15 sein!", Toast.LENGTH_SHORT).show()
            return null
        }

        // check gewicht
        val checkedGewicht: Float? = gewicht.toFloatOrNull()
        if (checkedGewicht == null || checkedGewicht <= 0) {
            Toast.makeText(
                this,
                "Gewicht darf nicht 0 oder kleiner sein! Eingabe mit Punkt statt Komma!",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }

        // check art
        if (art != "Klausur" && art != "Normale Note") {
            Toast.makeText(this, "Die Art muss Klausur oder Normale Note sein!", Toast.LENGTH_SHORT)
                .show()
            return null
        }

        return Note(
            0,
            date,
            checkedNote,
            checkedGewicht,
            art,
            bemerkung,
            intent.extras!!.getInt("id")
        )
    }
}