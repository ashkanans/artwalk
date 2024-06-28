package io.ashkanans.artwalk.presentation.library.dashboard.wikipedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.ashkanans.artwalk.R
import io.ashkanans.artwalk.domain.repository.wikipedia.Section


class SectionsAdapter(private val sections: List<Section>) :
    RecyclerView.Adapter<SectionsAdapter.SectionViewHolder>() {

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sectionHeader: TextView = itemView.findViewById(R.id.sectionHeader)
        val sectionText: TextView = itemView.findViewById(R.id.sectionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_section_wk, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]
        holder.sectionHeader.text = section.header
        holder.sectionText.text = section.text
    }

    override fun getItemCount(): Int = sections.size
}