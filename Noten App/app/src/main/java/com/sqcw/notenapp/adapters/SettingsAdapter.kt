package com.sqcw.notenapp.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.sqcw.notenapp.R
import com.sqcw.notenapp.db.NotenAppDao
import com.sqcw.notenapp.db.entities.Fach
import com.sqcw.notenapp.util.updateDb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SettingsAdapter(private val db: NotenAppDao) :
    ListAdapter<Fach, SettingsAdapter.SettingsViewHolder>(SettingsDiffCallback) {

    /* ViewHolder for Items, takes in the inflated view and the onClick behavior. */
    inner class SettingsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val cardColor = itemView.findViewById<CardView>(R.id.settingsFachCard)
        private val changeHintergrund = itemView.findViewById<TextView>(R.id.changeHintergrundFarbe)
        private val changeText =
            itemView.findViewById<TextView>(R.id.changeTextFarbe)
        private val fachNameInput = itemView.findViewById<EditText>(R.id.settingsFachName)
        private val klausurenGewicht = itemView.findViewById<EditText>(R.id.settingsKlausurGewicht)
        private val sonstigeGewicht = itemView.findViewById<EditText>(R.id.settingsSonstigeGewicht)
        private val deleteIcon = itemView.findViewById<ImageView>(R.id.settingsFachDeleteIcon)
        private val deleteText = itemView.findViewById<TextView>(R.id.settingsFachDeleteText)
        private val profilFachCheckBox = itemView.findViewById<CheckBox>(R.id.checkBoxProfilfach)
        private val pflichtFachCheckBox = itemView.findViewById<CheckBox>(R.id.checkBoxPflichtNote)

        /* Bind properties */
        @SuppressLint("ClickableViewAccessibility")
        fun bind(fach: Fach) {
            // delete
            deleteIcon.setOnClickListener {
                createDeleteDialog(fach, itemView.context)
            }
            deleteText.setOnClickListener {
                createDeleteDialog(fach, itemView.context)
            }

            // Gewichte
            klausurenGewicht.apply {
                setText(fach.klausurenGewicht.toString())

                doOnTextChanged { text, _, _, _ ->
                    val newWeight = text.toString().toIntOrNull() ?: return@doOnTextChanged
                    fach.klausurenGewicht = newWeight
                }
            }

            sonstigeGewicht.apply {
                setText(fach.sonstigeGewicht.toString())

                doOnTextChanged { text, _, _, _ ->
                    val newWeight = text.toString().toIntOrNull() ?: return@doOnTextChanged
                    fach.sonstigeGewicht = newWeight
                }
            }

            // set checkbox profilfach
            profilFachCheckBox.apply {
                isChecked = fach.profilFach

                setOnClickListener {
                    fach.profilFach = isChecked
                }
            }

            // set checkbox profilfach
            pflichtFachCheckBox.apply {
                isChecked = fach.pflichtFach

                setOnClickListener {
                    fach.pflichtFach = isChecked
                }
            }


            // handle upper Card and background color change
            cardColor.apply {
                setCardBackgroundColor(fach.farbeHintergrund)
            }

            // text color of upper card
            changeHintergrund.apply {
                setTextColor(fach.farbeText)

                setOnClickListener {
                    // Create Flag
                    val bubbleFlag = BubbleFlag(context)
                    bubbleFlag.flagMode = FlagMode.ALWAYS

                    // Create Custom Dialog
                    val dialog = ColorPickerDialog.Builder(context)
                        .setTitle("Hintergrundfarbe auswählen")
                        .attachAlphaSlideBar(false)
                        .attachBrightnessSlideBar(true)
                        .setPositiveButton("BESTÄTIGEN",
                            ColorEnvelopeListener { envelope, _ ->
                                fach.farbeHintergrund = envelope.color
                                cardColor.setCardBackgroundColor(envelope.color)
                            })

                        .setNegativeButton(
                            "ABBRECHEN"
                        ) { dialog, _ -> dialog.dismiss() }

                    // set Flag to Dialog and Show it
                    dialog.colorPickerView.flagView = bubbleFlag
                    dialog.colorPickerView.setInitialColor(fach.farbeHintergrund)
                    dialog.show()
                }
            }

            // text color of lower card
            changeText.apply {
                setTextColor(fach.farbeText)

                setOnClickListener {
                    // Create Flag
                    val bubbleFlag = BubbleFlag(context)
                    bubbleFlag.flagMode = FlagMode.ALWAYS

                    // Create Custom Dialog
                    val dialog = ColorPickerDialog.Builder(context)
                        .setTitle("Textfarbe auswählen")
                        .attachAlphaSlideBar(false)
                        .attachBrightnessSlideBar(true)
                        .setPositiveButton("BESTÄTIGEN",
                            ColorEnvelopeListener { envelope, _ ->
                                fach.farbeText = envelope.color
                                setTextColor(envelope.color)
                                changeHintergrund.setTextColor(envelope.color)
                            })

                        .setNegativeButton(
                            "ABBRECHEN"
                        ) { dialog, _ -> dialog.dismiss() }

                    // set Flag to Dialog and Show it
                    dialog.colorPickerView.flagView = bubbleFlag
                    dialog.colorPickerView.setInitialColor(fach.farbeText)
                    dialog.show()
                }
            }

            // handle name changes
            fachNameInput.apply {
                setText(fach.name)

                // Change item name
                doOnTextChanged { text, _, _, _ ->
                    fach.name = text.toString()
                }
            }
        }

        private suspend fun submitNewList(fach: Fach) {
            val newItems = db.getHalbjahrMitFaecher(fach.halbjahr)
            val toSubmit = if (newItems.isEmpty()) emptyList() else newItems[0].faecher
            submitList(toSubmit.reversed())
        }

        // Dialog for delete
        private fun createDeleteDialog(fach: Fach, context: Context) {
            val dialog = AlertDialog.Builder(context).create()
            dialog.setTitle("Löschen")
            dialog.setMessage("Möchtest du das Fach ${fach.name} in ${fach.halbjahr} löschen?")
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "BESTÄTGEN") { dia, _ ->
                GlobalScope.launch {
                    currentList.forEach { db.updateFach(it) }
                    db.deleteFach(fach)

                    // update database to make sure that everything is consistent after delete
                    updateDb(context, fach.copy(id = -1), fach.halbjahr)

                    // notify list about deleted item
                    submitNewList(fach)
                }
                dia.dismiss()
            }
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "ABBRECHEN") { dia, _ ->
                dia.dismiss()
            }
            dialog.show()
        }
    }

    /* Default function to create ViewHolder with correct Layout. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.settings_fach, parent, false)
        return SettingsViewHolder(view)
    }

    /* Binds ViewHolder. */
    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val fach = getItem(position)
        holder.bind(fach)
    }

    // Callback to efficiently compare items
    object SettingsDiffCallback : DiffUtil.ItemCallback<Fach>() {
        override fun areItemsTheSame(oldItem: Fach, newItem: Fach): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Fach, newItem: Fach): Boolean {
            return oldItem == newItem
        }
    }
}