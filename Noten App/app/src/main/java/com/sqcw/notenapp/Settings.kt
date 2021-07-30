package com.sqcw.notenapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sqcw.notenapp.db.NotenAppDao
import com.sqcw.notenapp.db.NotenAppDatabase
import kotlinx.coroutines.launch

class Settings : AppCompatActivity() {
    private val halbJahre = arrayOf("12/1", "12/2", "13/1", "13/2", "Abitur")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeDropdown()

        //initialize onCloseListener
        findViewById<ImageView>(R.id.settingsCloseIcon).apply {
            setOnClickListener { finish() }
        }

        // get Database and initialize all values
        val db = NotenAppDatabase.getInstance(this).dao()
        lifecycleScope.launch {
            init(db)
        }
    }

    // initialize Halbjahr dropdown
    private fun initializeDropdown() {
        findViewById<AutoCompleteTextView>(R.id.settingsHalbjahr).apply {
            keyListener = null
            setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    halbJahre
                )
            )
        }
    }

    private suspend fun init(db: NotenAppDao) {
        val metaInformation = db.readMetaInformation()[0]

        // set Name text
        if (metaInformation.name != "") {
            findViewById<EditText>(R.id.settingsName).apply {
                setText(metaInformation.name)
            }
        }

        // set halbjahr text
        findViewById<AutoCompleteTextView>(R.id.settingsHalbjahr).apply {
            setText(metaInformation.halbjahr, false)
        }

        // initialize Accept - update metaInformation and update Fächer
        findViewById<ImageView>(R.id.settingsAccept).apply {
            setOnClickListener {
                lifecycleScope.launch {
                    val metaInformationUpdated = updateMetaInformation(db)
                    val faecherUpdated = updateFaecherInHalbjahr(db)

                    // notify user if something changed
                    if (metaInformationUpdated || faecherUpdated)
                        Toast.makeText(
                            applicationContext,
                            "Einstellungen geändert!",
                            Toast.LENGTH_SHORT
                        ).show()

                    // close activity anyway
                    finish()
                }
            }
        }
    }

    private suspend fun updateMetaInformation(db: NotenAppDao): Boolean {
        val currentMetaInformation = db.readMetaInformation()[0]
        val inputName = findViewById<EditText>(R.id.settingsName).text.toString()
        val inputHalbjahr =
            findViewById<AutoCompleteTextView>(R.id.settingsHalbjahr).text.toString()

        // only update if values differ
        if (currentMetaInformation.name == inputName && currentMetaInformation.halbjahr == inputHalbjahr)
            return false

        val newMetaInformation =
            currentMetaInformation.copy(name = inputName, halbjahr = inputHalbjahr)
        db.updateMetaInformation(newMetaInformation)
        return true
    }

    private suspend fun updateFaecherInHalbjahr(db: NotenAppDao): Boolean {
        return false
    }
}