package com.sqcw.notenapp.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.R
import com.sqcw.notenapp.db.NotenAppDao
import com.sqcw.notenapp.db.entities.Fach
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import yuku.ambilwarna.AmbilWarnaDialog

class SettingsAdapter(private val db: NotenAppDao) :
    ListAdapter<Fach, SettingsAdapter.SettingsViewHolder>(SettingsDiffCallback) {

    /* ViewHolder for Items, takes in the inflated view and the onClick behavior. */
    inner class SettingsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val card = itemView.findViewById<CardView>(R.id.settingsFachCard)
        private val fachNameInput = itemView.findViewById<EditText>(R.id.settingsFachName)
        private val gewicht = itemView.findViewById<EditText>(R.id.settingsFachGewicht)

        /* Bind properties */
        @SuppressLint("ClickableViewAccessibility")
        fun bind(fach: Fach) {
            // handle color
            card.apply {
                setCardBackgroundColor(fach.farbe)

                setOnClickListener {
                    val dialog = AmbilWarnaDialog(context, fach.farbe, object :
                        AmbilWarnaDialog.OnAmbilWarnaListener {
                        override fun onCancel(dialog: AmbilWarnaDialog?) {}

                        override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                            GlobalScope.launch {
                                // remove from db
                                db.updateFach(fach.copy(farbe = color))

                                // assign new values
                                val dbList = db.getHalbjahrMitFaecher(fach.halbjahr)
                                val newList =
                                    if (dbList.isEmpty()) emptyList() else dbList[0].faecher.reversed()
                                submitList(newList)
                            }
                        }
                    })
                    dialog.show()
                }
            }

            // handle weight changes
            gewicht.apply {
                setText(fach.gewicht.toString())

                doOnTextChanged { text, _, _, _ ->
                    // check gewicht
                    val input = text.toString()
                    if (input == "") return@doOnTextChanged

                    val checkedGewicht: Int? = input.toIntOrNull()
                    if (checkedGewicht == null || checkedGewicht <= 0) {
                        Toast.makeText(
                            context,
                            "Gewicht muss eine ganze Zahl > 0 sein!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@doOnTextChanged
                    }
                    fach.gewicht = checkedGewicht
                }
            }

            // handle name changes
            fachNameInput.apply {
                setText(fach.name)

                // Change item name
                doOnTextChanged { text, _, _, _ ->
                    fach.name = text.toString()
                }

                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        if (event.rawX >= (right - compoundDrawables[2].bounds.width())) {
                            // your action here

                            // open Dialog
                            val alertDialog = AlertDialog.Builder(context).create()
                            alertDialog.setTitle("Löschen")
                            alertDialog.setMessage("Fach ${fach.name} in Abschnitt ${fach.halbjahr} löschen?")

                            // remove
                            alertDialog.setButton(
                                AlertDialog.BUTTON_POSITIVE,
                                "BESTÄTIGEN"
                            ) { dialog, _ ->
                                GlobalScope.launch {
                                    // remove from db
                                    db.deleteFach(fach)

                                    // assign new values
                                    val dbList = db.getHalbjahrMitFaecher(fach.halbjahr)
                                    val newList =
                                        if (dbList.isEmpty()) emptyList() else dbList[0].faecher.reversed()
                                    submitList(newList)
                                }
                                dialog.dismiss()
                            }

                            // close dialog and no changes
                            alertDialog.setButton(
                                AlertDialog.BUTTON_NEGATIVE,
                                "ABBRECHEN"
                            ) { dialog, _ -> dialog.dismiss() }

                            // show dialog
                            alertDialog.show()
                            return@setOnTouchListener true
                        }
                    }
                    return@setOnTouchListener false
                }
            }
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