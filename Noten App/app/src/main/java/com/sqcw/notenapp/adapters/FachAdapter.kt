package com.sqcw.notenapp.adapters

import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.*
import com.sqcw.notenapp.AddNeueNote
import com.sqcw.notenapp.R
import com.sqcw.notenapp.db.relations.FachAndNoten


class FachAdapter :
    ListAdapter<FachAndNoten, FachAdapter.FachAndNotenViewHolder>(FachAndNotenDiffCallback) {
    private var expanded: MutableList<Boolean> = mutableListOf()

    /* ViewHolder for Items, takes in the inflated view and the onClick behavior. */
    inner class FachAndNotenViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val card = itemView.findViewById<CardView>(R.id.fachCard)
        private val name = itemView.findViewById<TextView>(R.id.fachName)
        private val endNote = itemView.findViewById<TextView>(R.id.fachEndnote)
        private val schnitt = itemView.findViewById<TextView>(R.id.fachSchnitt)
        private val addNote = itemView.findViewById<ImageView>(R.id.fachAdd)
        private val notenListe = itemView.findViewById<RecyclerView>(R.id.fachNoten)


        /* Bind properties */
        fun bind(fachAndNoten: FachAndNoten, position: Int, onClick: View.OnClickListener) {
            itemView.setOnClickListener(onClick)

            // data
            val fach = fachAndNoten.fach
            val noten = fachAndNoten.noten

            // all values
            card.setCardBackgroundColor(Color.parseColor(fach.farbe))
            name.text = fach.name
            endNote.text = "${if (fach.beinhaltetNoten) fach.endnote else "N/A"} Punkte"

            // set Padding at the bottom according to current state
            schnitt.apply {
                text = if (fach.beinhaltetNoten) "%.2f".format(fach.schnitt) else "N/A"

                // set Padding in Dp
                setPadding(
                    0, 0, 0,
                    if (expanded[position])
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            0f,
                            resources.displayMetrics
                        ).toInt()
                    else
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            13f,
                            resources.displayMetrics
                        ).toInt()
                )
            }

            // switch to new screen
            addNote.setOnClickListener {
                val intent = Intent(itemView.context, AddNeueNote::class.java)
                intent.putExtra("id", fach.id)
                itemView.context.startActivity(intent)
            }

            // set up notenListe
            notenListe.apply {
                visibility = if (expanded[position]) View.VISIBLE else View.GONE
                layoutManager = LinearLayoutManager(context)

                // set adapter
                val concatAdapter = ConcatAdapter()
                adapter = concatAdapter

                // filter for Noten
                val klausurNoten = noten.filter { note -> note.art == "Klausur" }
                val sonstigeNoten = noten.filter { note -> note.art == "Normale Note" }

                // add klausuren headline and Noten if there are any
                if (klausurNoten.isNotEmpty()) {
                    // initialize Adapters with data
                    val headlineKlausurenAdapter =
                        NotenHeaderAdapter(context, "Klausuren", fach.klausurenSchnitt)

                    // create Adapter and set data for Klausurnoten
                    val klausurenAdapter = NotenAdapter()
                    klausurenAdapter.submitList(klausurNoten)

                    // add Adapters
                    concatAdapter.addAdapter(headlineKlausurenAdapter)
                    concatAdapter.addAdapter(klausurenAdapter)
                }

                if (sonstigeNoten.isNotEmpty()) {
                    // initialize Adapters with data
                    val headlineNormaleNotenAdapter =
                        NotenHeaderAdapter(context, "Normale Noten", fach.sonstigeSchnitt)

                    // create Adapter and set data for normale Noten
                    val normaleNotenAdapter = NotenAdapter()
                    normaleNotenAdapter.submitList(sonstigeNoten)

                    // add Adapters
                    concatAdapter.addAdapter(headlineNormaleNotenAdapter)
                    concatAdapter.addAdapter(normaleNotenAdapter)
                }
                concatAdapter.notifyDataSetChanged()
            }
        }
    }

    /* Creates and inflates view and return ViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FachAndNotenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fach, parent, false)
        return FachAndNotenViewHolder(view)
    }

    /* Gets current item and uses it to bind view. */
    override fun onBindViewHolder(holder: FachAndNotenViewHolder, position: Int) {
        val fachAndNoten = getItem(position)
        holder.bind(fachAndNoten, position) {
            expanded[position] = !expanded[position]
            notifyItemChanged(position)
        }
    }

    // Callback to efficiently compare items
    object FachAndNotenDiffCallback : DiffUtil.ItemCallback<FachAndNoten>() {
        override fun areItemsTheSame(oldItem: FachAndNoten, newItem: FachAndNoten): Boolean {
            return oldItem.fach.id == newItem.fach.id
        }

        override fun areContentsTheSame(oldItem: FachAndNoten, newItem: FachAndNoten): Boolean {
            return oldItem == newItem
        }
    }

    // override this function to work properly with expansions
    override fun submitList(list: MutableList<FachAndNoten>?) {
        super.submitList(list)

        // create new List with the right size
        expanded = if (list == null) mutableListOf() else MutableList(list.size) { false }
    }
}