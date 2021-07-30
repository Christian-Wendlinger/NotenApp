package com.sqcw.notenapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.sqcw.notenapp.ChangeNote
import com.sqcw.notenapp.R
import com.sqcw.notenapp.db.entities.Note

class NotenAdapter :
    ListAdapter<Note, NotenAdapter.NotenViewHolder>(NotenDiffCallback) {
    /* ViewHolder for Items, takes in the inflated view and the onClick behavior. */
    inner class NotenViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val date = itemView.findViewById<TextView>(R.id.noteDatum)
        private val punktzahl = itemView.findViewById<TextView>(R.id.notePunktzahl)
        private val bemerkung = itemView.findViewById<TextView>(R.id.noteBemerkung)

        /* Bind properties */
        fun bind(note: Note, onLongClick: View.OnLongClickListener) {
            itemView.setOnLongClickListener(onLongClick)

            // initialize values
            date.text = note.datum
            punktzahl.text =
                "${if (note.gewicht != 1f) "(x${note.gewicht})" else ""}    ${note.punktzahl}"
            bemerkung.text = note.bemerkung
        }
    }

    /* Creates and inflates view and return ViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.noten, parent, false)
        return NotenViewHolder(view)
    }

    /* Gets current item and uses it to bind view. */
    override fun onBindViewHolder(holder: NotenViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note) {
            holder.itemView.apply {
                val intent = Intent(context, ChangeNote::class.java)
                intent.putExtra("id", note.id)
                context.startActivity(intent)
            }
            return@bind true
        }
    }

    // Callback to efficiently compare items
    object NotenDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}