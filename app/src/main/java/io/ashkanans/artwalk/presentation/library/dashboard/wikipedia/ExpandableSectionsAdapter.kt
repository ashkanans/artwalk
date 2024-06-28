package io.ashkanans.artwalk.presentation.library.dashboard.wikipedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.domain.repository.wikipedia.HeaderItem

class ExpandableSectionsAdapter(private val headerItems: List<HeaderItem>) :
    RecyclerView.Adapter<ExpandableSectionsAdapter.HeaderViewHolder>() {

    private var expandedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val headerItem = headerItems[position]
        holder.bind(headerItem, position)
    }

    override fun getItemCount(): Int = headerItems.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTitle: TextView = itemView.findViewById(R.id.headerTitle)
        private val sectionRecyclerView: RecyclerView =
            itemView.findViewById(R.id.sectionRecyclerView)

        fun bind(headerItem: HeaderItem, position: Int) {
            headerTitle.text = headerItem.title

            // Determine if this item is expanded or collapsed
            val isExpanded = position == expandedPosition
            sectionRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // Set up the sections RecyclerView
            sectionRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            sectionRecyclerView.adapter = SectionsAdapter(headerItem.sectionList)

            // Toggle visibility of sectionRecyclerView on header click
            itemView.setOnClickListener {
                expandedPosition = if (isExpanded) -1 else position
                notifyDataSetChanged()
            }
        }
    }
}
