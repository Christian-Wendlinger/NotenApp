package com.sqcw.notenapp.adapters


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.AddNeueNote
import com.sqcw.notenapp.R
import com.sqcw.notenapp.data.TheViewModel
import com.sqcw.notenapp.data.entities.Fach

class FachListeAdapter internal constructor(
    context: Context?,
    db: TheViewModel
) :
    RecyclerView.Adapter<FachListeAdapter.ViewHolder>() {

    private val db = db
    private var items: List<Fach> = emptyList()
    private var expanded: MutableList<Boolean> = mutableListOf()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // inflates the cell layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            inflater.inflate(
                R.layout.fach_list_item,
                parent,
                false
            )
        )
    }

    // binds the data to the TextView in each cell
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fach: Fach = items[position]

        holder.itemView.apply {
            // set Fachname
            findViewById<TextView>(R.id.fachListeFachname).apply { text = fach.name }

            // set Endnote
            findViewById<TextView>(R.id.fachListeEndnote).apply {
                text = "${if (fach.beinhaltetNoten) fach.endnote else "NA"} Punkte"
            }

            // set Schnitt
            findViewById<TextView>(R.id.fachListeSchnitt).apply {
                text = "${if (fach.beinhaltetNoten) fach.schnitt else "NA"}"
            }

            // set backgroundColor of Card
            findViewById<CardView>(R.id.fachListeCard).apply {
                setCardBackgroundColor(
                    Color.parseColor(
                        fach.farbe
                    )
                )
            }

            // add neue Note Listener
            findViewById<ImageView>(R.id.fachListeAddIcon).apply {
                setOnClickListener {
                    val intent = Intent(context, AddNeueNote::class.java)
                    intent.putExtra("id", fach.id)
                    context.startActivity(intent)
                }
            }

            // initialize NotenListe
            findViewById<RecyclerView>(R.id.fachListeNotenListe).apply {
                visibility = if (expanded[position]) View.VISIBLE else View.GONE

                layoutManager = LinearLayoutManager(context)

                // create all adapters and data
                val headlineKlausuren = NotenListeHeaderAdapter(context, "Klausuren")
                val headlineNormaleNoten = NotenListeHeaderAdapter(context, "Normale Noten")
                val klausuren = NotenListeNotenAdapter(context)
                val normaleNoten = NotenListeNotenAdapter(context)
                var adapterList =
                    mutableListOf(headlineKlausuren, klausuren, headlineNormaleNoten, normaleNoten)
                adapter =
                    ConcatAdapter(adapterList)

                // update noten Data
                // read Data from database
                db.getFachMitNoten(fach.id)
                    .observe(context as LifecycleOwner, { data ->
                        val filteredData = data[0].noten.filter { note -> note.art == "Klausur" }
                        (klausuren).setData(filteredData)

                        if (filteredData.isEmpty()) {
                            (adapter as ConcatAdapter).removeAdapter(headlineKlausuren)
                            (adapter as ConcatAdapter).removeAdapter(klausuren)
                            (adapter as ConcatAdapter).notifyDataSetChanged()
                        }
                    })

                db.getFachMitNoten(fach.id)
                    .observe(context as LifecycleOwner, { data ->
                        val filteredData =
                            data[0].noten.filter { note -> note.art == "Normale Note" }
                        (normaleNoten).setData(filteredData)

                        if (filteredData.isEmpty()) {
                            (adapter as ConcatAdapter).removeAdapter(headlineNormaleNoten)
                            (adapter as ConcatAdapter).removeAdapter(normaleNoten)
                            (adapter as ConcatAdapter).notifyDataSetChanged()
                        }
                    })
            }

            // listener to expand and contract list items
            setOnClickListener {
                expanded[position] = !expanded[position]
                notifyItemChanged(position)
            }
        }
    }

    // total number of cells
    override fun getItemCount(): Int {
        return items.size
    }

    // update data
    fun setData(faecher: List<Fach>) {
        items = faecher
        expanded = MutableList(faecher.size) { false }
        notifyDataSetChanged()
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)
}