package com.sqcw.notenapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.adapters.SettingsAdapter
import com.sqcw.notenapp.db.NotenAppDao
import com.sqcw.notenapp.db.NotenAppDatabase
import com.sqcw.notenapp.db.entities.Fach
import kotlinx.coroutines.launch

class Settings : AppCompatActivity() {
    private val halbJahre = arrayOf("12/1", "12/2", "13/1", "13/2", "Abitur")
    private var currentUsername = ""
    private lateinit var settingsAdapter: SettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // get Database and adapter
        val db = NotenAppDatabase.getInstance(this).dao()
        settingsAdapter = SettingsAdapter(db)

        // initialize rest
        lifecycleScope.launch {
            init(db)
            initializeDropdown(db)
        }
    }

    // initialize Halbjahr dropdown
    private suspend fun initializeDropdown(db: NotenAppDao) {
        findViewById<AutoCompleteTextView>(R.id.settingsHalbjahr).apply {
            keyListener = null
            setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    halbJahre
                )
            )

            setOnItemClickListener { _, _, pos, _ ->
                // update metaInformation
                lifecycleScope.launch {
                    // Change Views
                    val newMetaInformation =
                        db.readMetaInformation()[0].copy(halbjahr = adapter.getItem(pos).toString())
                    rootView.findViewById<TextView>(R.id.settingsFaecherlisteTitle).apply {
                        text = "Fächer in ${newMetaInformation.halbjahr}"
                    }

                    // update Info in DB
                    db.updateMetaInformation(newMetaInformation)
                    updateFachListe(db)

                    // Notify User about change
                    Toast.makeText(
                        applicationContext,
                        "Halbjahr für Bearbeitung auf ${newMetaInformation.halbjahr} gewechselt!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun init(db: NotenAppDao) {
        val metaInformation = db.readMetaInformation()[0]

        // set Name text
        currentUsername = metaInformation.name
        findViewById<EditText>(R.id.settingsName).apply {
            setText(metaInformation.name)

            // update username when changed
            doOnTextChanged { text, _, _, _ ->
                currentUsername = text.toString()
            }
        }

        // set halbjahr + halbjahr text
        findViewById<AutoCompleteTextView>(R.id.settingsHalbjahr).apply {
            setText(metaInformation.halbjahr, false)
        }

        // initialize FachListe Headline
        findViewById<TextView>(R.id.settingsFaecherlisteTitle).apply {
            text = "Fächer in ${metaInformation.halbjahr} "
        }

        // initialize Fachliste
        findViewById<RecyclerView>(R.id.settingsFachliste).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
            updateFachListe(db)
        }

        // add neues Fach
        findViewById<ImageView>(R.id.settingsAddFach).apply {
            setOnClickListener {
                // add new item to temporary list and update recyclerview
                lifecycleScope.launch {
                    db.insertFach(
                        Fach(
                            halbjahr = db.readMetaInformation()[0].halbjahr
                        )
                    )
                    updateFachListe(db)
                }
            }
        }

        //initialize onCloseListener = save
        findViewById<ImageView>(R.id.settingsBackIcon).apply {
            setOnClickListener {
                lifecycleScope.launch {
                    // update current metaInformation
                    val newMetaInformation =
                        db.readMetaInformation()[0].copy(name = currentUsername)
                    db.updateMetaInformation(newMetaInformation)

                    // update fachListe
                    val currentItems = settingsAdapter.currentList
                    currentItems.forEach { db.updateFach(it) }

                    // notify user about changes and close
                    Toast.makeText(
                        applicationContext,
                        "Einstellungen gespeichert!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private suspend fun updateFachListe(db: NotenAppDao) {
        // read data and reverse it, so that the newest one is on top
        val metaInformation = db.readMetaInformation()[0]
        val data = db.getHalbjahrMitFaecher(metaInformation.halbjahr)
        val newList = if (data.isEmpty()) emptyList() else data[0].faecher
        settingsAdapter.submitList(newList.reversed())
    }
}