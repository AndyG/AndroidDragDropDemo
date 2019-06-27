package com.discord.androiddragdropdemo

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NumberItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    private val textView: TextView = view.findViewById(R.id.draggable_view_text)

    fun configure(dragAndDropNumberItem: DragAndDropNumberItem) {
        val text = "Item number: ${dragAndDropNumberItem.number}"
        this.textView.text = text
    }
}
