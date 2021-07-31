package com.sqcw.notenapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.R

class NotenHeaderAdapter internal constructor(
    context: Context?,
    title: String,
    schnitt: Float,
    color: Int
) :
    RecyclerView.Adapter<NotenHeaderAdapter.ViewHolder>() {
    private val title: String = title
    private var schnitt: Float = schnitt
    private val color: Int = color
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // inflates the cell layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            inflater.inflate(
                R.layout.noten_headline,
                parent,
                false
            )
        )
    }

    // binds the data to the TextView in each cell
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Change title
        holder.itemView.apply {
            findViewById<TextView>(R.id.notenHeadline).apply {
                text = "$title — ${"%.2f".format(schnitt)}"
                setTextColor(color)
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