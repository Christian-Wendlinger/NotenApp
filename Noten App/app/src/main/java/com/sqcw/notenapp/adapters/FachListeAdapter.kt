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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sqcw.notenapp.AddNeueNote
import com.sqcw.notenapp.R
import com.sqcw.notenapp.data.entities.Fach
import com.sqcw.notenapp.data.entities.Note

class FachListeAdapter internal constructor(
    context: Context?,
    faecher: List<Fach>,
    notenSammlung: List<List<Note>>
) :
    RecyclerView.Adapter<FachListeAdapter.ViewHolder>() {

    private val faecher: List<Fach> = faecher
    private val notenSammlung: List<List<Note>> = notenSammlung
    private var expanded: MutableList<Boolean> = MutableList(faecher.size) { false }
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
        val fach = faecher[position]
        val noten: List<Note> = notenSammlung[position]

        holder.itemView.apply {
            // set backgroundColor of Card
            findViewById<CardView>(R.id.fachListeCard).apply {
                setCardBackgroundColor(
                    Color.parseColor(
                        fach.farbe
                    )
                )
            }

            // set Fachname
            findViewById<TextView>(R.id.fachListeFachname).apply { text = fach.name }

            // set Endnote
            findViewById<TextView>(R.id.fachListeEndnote).apply {
                text = "${if (fach.beinhaltetNoten) fach.endnote else "N/A"} Punkte"
            }

            // set Schnitt
            findViewById<TextView>(R.id.fachListeSchnitt).apply {
                text = if (fach.beinhaltetNoten) "%.2f".format(fach.schnitt) else "N/A"
            }

            // listener to expand and contract list items
            setOnClickListener {
                expanded[position] = !expanded[position]
                notifyItemChanged(position)
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
                adapter = ConcatAdapter()

                // filter for Noten
                val klausurNoten = noten.filter { note -> note.art == "Klausur" }
                val sonstigeNoten = noten.filter { note -> note.art == "Normale Note" }

                // add klausuren headline and Noten if there are any
                if (klausurNoten.isNotEmpty()) {
                    // initialize Adapters with data
                    val headlineKlausuren =
                        NotenListeHeaderAdapter(context, "Klausuren", fach.klausurenSchnitt)
                    val klausuren = NotenListeNotenAdapter(context, klausurNoten)

                    // add Adapters
                    (adapter as ConcatAdapter).addAdapter(headlineKlausuren)
                    (adapter as ConcatAdapter).addAdapter(klausuren)
                    (adapter as ConcatAdapter).notifyDataSetChanged()
                }

                if (sonstigeNoten.isNotEmpty()) {
                    // initialize Adapters with data
                    val headlineNormaleNoten =
                        NotenListeHeaderAdapter(context, "Normale Noten", fach.sonstigeSchnitt)
                    val normaleNoten = NotenListeNotenAdapter(context, sonstigeNoten)

                    // add Adapters
                    (adapter as ConcatAdapter).addAdapter(headlineNormaleNoten)
                    (adapter as ConcatAdapter).addAdapter(normaleNoten)
                    (adapter as ConcatAdapter).notifyDataSetChanged()
                }
            }
        }
    }

    // total number of cells
    override fun getItemCount(): Int {
        return faecher.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)
}