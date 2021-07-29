package com.sqcw.notenapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.R

class NotenListeHeaderAdapter internal constructor(
    context: Context?,
    title: String,
    schnitt: Float
) :
    RecyclerView.Adapter<NotenListeHeaderAdapter.ViewHolder>() {
    private val title: String = title
    private var schnitt: Float = schnitt
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // inflates the cell layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            inflater.inflate(
                R.layout.noten_liste_headline,
                parent,
                false
            )
        )
    }

    // binds the data to the TextView in each cell
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Change title
        holder.itemView.apply {
            findViewById<TextView>(R.id.notenListeHeadline).apply {
                text = "$title — ${"%.2f".format(schnitt)}"
            }
        }
    }

    // total number of cells
    override fun getItemCount(): Int {
        return 1
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)
}