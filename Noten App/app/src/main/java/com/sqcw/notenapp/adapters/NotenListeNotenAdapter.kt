package com.sqcw.notenapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.ChangeNote
import com.sqcw.notenapp.R
import com.sqcw.notenapp.data.entities.Note

class NotenListeNotenAdapter internal constructor(
    context: Context?
) :
    RecyclerView.Adapter<NotenListeNotenAdapter.ViewHolder>() {
    private var items: List<Note> = emptyList()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // inflates the cell layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            inflater.inflate(
                R.layout.noten_liste_item,
                parent,
                false
            )
        )
    }

    // binds the data to the TextView in each cell
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = items[position]

        holder.itemView.apply {
            // change date
            findViewById<TextView>(R.id.notenListeDatum).apply {
                text = note.datum
            }

            // change Punktzahl
            findViewById<TextView>(R.id.notenListeNote).apply {
                text = "${if (note.gewicht > 1) "(x${note.gewicht})" else ""}    ${note.punktzahl}"
            }

            // change Bemerkung
            findViewById<TextView>(R.id.notenListeBemerkung).apply {
                text = note.bemerkung
            }

            // Listener
            setOnLongClickListener {
                val intent = Intent(context, ChangeNote::class.java)
                intent.putExtra("id", note.id)
                context.startActivity(intent)

                return@setOnLongClickListener true
            }
        }
    }

    // total number of cells
    override fun getItemCount(): Int {
        return items.size
    }

    // update data
    fun setData(noten: List<Note>) {
        items = noten
        notifyDataSetChanged()
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)
}