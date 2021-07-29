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

class ChangeNote : AppCompatActivity() {

    private val notenArten = arrayOf("Normale Note", "Klausur")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_note)

        // get Database Instance
        val db = ViewModelProvider(this).get(TheViewModel::class.java)

        // initialize Dropdown values
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

        // initialize all Fields
        db.readNote(intent.extras!!.getInt("id")).observe(this, Observer { data ->
            if (data.isEmpty()) return@Observer

            val note = data[0]

            // Fach name
            db.readFach(note.fachId)
                .observe(this, Observer { fachListe ->
                    if (fachListe.isEmpty()) return@Observer

                    val fach = fachListe[0]
                    findViewById<TextView>(R.id.changeNoteTitel).apply { text = fach.name }
                })

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
                setText(
                    note.art,
                    false
                )
            }
            //Bemerkung
            findViewById<EditText>(R.id.changeNoteBemerkung).apply { setText(note.bemerkung) }
        })

        // initalize Delete Listener
        findViewById<ImageView>(R.id.changeNoteDeleteIcon).apply {
            setOnClickListener {

                db.readNote(intent.extras!!.getInt("id"))
                    .observe(context as LifecycleOwner, Observer { notenListe ->
                        if (notenListe.isEmpty()) return@Observer

                        val note = notenListe[0]

                        // delete Note from database
                        db.deleteNote(note)

                        // update information for fach and halbjahr
                        updateFachInformation(
                            context as ViewModelStoreOwner,
                            context as LifecycleOwner,
                            note.fachId
                        )

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

                        // notify user that Note was deleted and close activity
                        rootView.findViewById<TextView>(R.id.changeNoteTitel).also { textView ->
                            Toast.makeText(
                                applicationContext,
                                "Note in ${textView.text} gelöscht!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        finish()
                    })
            }
        }


        // initialize Accept Listener
        findViewById<ImageView>(R.id.changeNoteAccept).apply {
            setOnClickListener {
                val note = parseInput() ?: return@setOnClickListener

                // read note from database, change missing fiels (id and fachid) and compare them to each other
                db.readNote(intent.extras!!.getInt("id"))
                    .observe(context as LifecycleOwner, Observer { notenListe ->
                        if (notenListe.isEmpty()) return@Observer

                        val currentNote = notenListe[0]
                        note.id = currentNote.id
                        note.fachId = currentNote.fachId

                        // update data in database and adjust Schnitte if anything changed and close activity
                        if (note != currentNote) {
                            db.updateNote(note)
                            updateFachInformation(
                                context as ViewModelStoreOwner,
                                context as LifecycleOwner,
                                note.fachId
                            )

                            db.readMetaInformation().observe(
                                context as LifecycleOwner,
                                Observer { metaInformationListe ->
                                    if (metaInformationListe.isEmpty()) return@Observer

                                    val metaInformation = metaInformationListe[0]
                                    updateHalbjahrInformation(
                                        context as ViewModelStoreOwner,
                                        context as LifecycleOwner,
                                        metaInformation.halbjahr
                                    )
                                })
                        }
                        finish()
                    })
            }
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