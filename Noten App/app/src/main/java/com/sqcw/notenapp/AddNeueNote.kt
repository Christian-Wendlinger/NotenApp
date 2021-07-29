package com.sqcw.notenapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sqcw.notenapp.data.TheDao
import com.sqcw.notenapp.data.TheDatabase
import com.sqcw.notenapp.data.entities.Note
import com.sqcw.notenapp.util.updateDb
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddNeueNote : AppCompatActivity() {

    private val notenArten = arrayOf("Normale Note", "Klausur")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neue_note)

        // initialize Database
        val db = TheDatabase.getInstance(this).dao()
        lifecycleScope.launch {
            init(db)
        }
    }

    private suspend fun init(db: TheDao) {
        val fachListe = db.readFach(intent.extras!!.getInt("id"))

        // This should never crash!
        val fach = fachListe[0]

        initializeDropdown()

        // set title
        findViewById<TextView>(R.id.neueNoteTitel).apply { text = fach.name }
        // set Gewicht as default to 1
        findViewById<EditText>(R.id.neueNoteGewicht).apply { setText("1") }
        // set Date as default to current Date
        findViewById<EditText>(R.id.neueNoteDatum).apply {
            val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
            setText(date)
        }


        // initialize close
        findViewById<ImageView>(R.id.neueNoteCloseIcon).apply { setOnClickListener { finish() } }

        // initialize Add
        findViewById<ImageView>(R.id.neueNoteAccept).apply {
            setOnClickListener {
                val note = parseInput() ?: return@setOnClickListener

                lifecycleScope.launch {
                    db.insertNote(note)
                    updateDb(context, fach, fach.halbjahr)

                    // close activity
                    finish()
                    // notify user about change and close Activity
                    Toast.makeText(
                        applicationContext,
                        "Neue Note in ${fach.name} hinzugefügt!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // set Dropdown values
    private fun initializeDropdown() {
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