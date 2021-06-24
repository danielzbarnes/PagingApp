package com.danielzbarnes.pagingapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danielzbarnes.pagingapp.R

// ViewHolder for Separator item
class SeparatorViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val description: TextView = view.findViewById(R.id.separator_desc)

    fun bind(separator: String){
        description.text = separator
    }

    companion object{

        fun create(parent: ViewGroup): SeparatorViewHolder{

            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_separator_view, parent, false)
            return SeparatorViewHolder(view)
        }
    }
}