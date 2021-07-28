package com.sqcw.notenapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity

class AddNeueNote : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neue_note)

        // initialize Dropdown values
        findViewById<AutoCompleteTextView>(R.id.neueNoteArt).apply {
            keyListener = null
            setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    arrayOf("Klausur", "Sonstige")
                )
            )
        }
    }
}