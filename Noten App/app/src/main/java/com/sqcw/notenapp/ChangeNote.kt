package com.sqcw.notenapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sqcw.notenapp.db.NotenAppDao
import com.sqcw.notenapp.db.NotenAppDatabase
import com.sqcw.notenapp.db.entities.Note
import com.sqcw.notenapp.util.updateDb
import kotlinx.coroutines.launch

class ChangeNote : AppCompatActivity() {

    private val notenArten = arrayOf("Normale Note", "Klausur")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_note)

        // initialize Art Dropdown
        initializeDropdown()

        // get Database Instance
        val db = NotenAppDatabase.getInstance(this).dao()
        lifecycleScope.launch {
            init(db)
        }
    }

    private suspend fun init(db: NotenAppDao) {
        // This should never crash!
        val note = db.readNote(intent.extras!!.getInt("id"))[0]
        val fach = db.readFach(note.fachId)[0]

        // initalize all values
        // Fachname
        findViewById<TextView>(R.id.changeNoteTitel).apply { text = fach.name }
        // datum
        findViewById<EditText>(R.id.changeNoteDatum).apply { setText(note.datum) }
        //note
        findViewById<EditText>(R.id.changeNoteNote).apply { setText(note.punktzahl.toString()) }
        //Gewicht
        findViewById<EditText>(R.id.changeNoteGewicht).apply {
            setText(
                note.gewicht.toString()
            )
        }
        //Art
        findViewById<AutoCompleteTextView>(R.id.changeNoteArt).apply {
            setText(note.art, false)
        }
        //Bemerkung
        findViewById<EditText>(R.id.changeNoteBemerkung).apply { setText(note.bemerkung) }

        // initialize delete
        findViewById<ImageView>(R.id.changeNoteDeleteIcon).apply {
            setOnClickListener {
                // delete from database
                lifecycleScope.launch {
                    db.deleteNote(note)
                    // update other data
                    updateDb(context, fach, fach.halbjahr)

                    // close activity
                    finish()
                    // notify user about change and close Activity
                    Toast.makeText(
                        applicationContext,
                        "Note in ${fach.name} gelöscht!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // initialize accept
        findViewById<ImageView>(R.id.changeNoteAccept).apply {
            setOnClickListener {
                var userInputNote = parseInput() ?: return@setOnClickListener
                userInputNote = userInputNote.copy(id = note.id, fachId = note.fachId)

                // nothing changed
                if (userInputNote == note) {
                    finish()
                    return@setOnClickListener
                }

                // update data
                lifecycleScope.launch {
                    db.updateNote(userInputNote)
                    updateDb(context, fach, fach.halbjahr)

                    // close activity
                    finish()
                    // notify user about change and close Activity
                    Toast.makeText(
                        applicationContext,
                        "Note in ${fach.name} geändert!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // initialize Dropdown values
    private fun initializeDropdown() {
        findViewById<AutoCompleteTextView>(R.id.changeNoteArt).apply {
            keyListener = null
            setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    notenArten
                )
            )
        }
    }

    // check input
    private fun parseInput(): Note? {
        val date = findViewById<EditText>(R.id.changeNoteDatum).text.toString()
        val note = findViewById<EditText>(R.id.changeNoteNote).text.toString()
        val gewicht = findViewById<EditText>(R.id.changeNoteGewicht).text.toString()
        val art = findViewById<EditText>(R.id.changeNoteArt).text.toString()
        val bemerkung = findViewById<EditText>(R.id.changeNoteBemerkung).text.toString()

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