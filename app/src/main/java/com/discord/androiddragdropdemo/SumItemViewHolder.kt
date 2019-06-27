package com.discord.androiddragdropdemo

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SumItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    private val textView: TextView = view.findViewById(R.id.draggable_view_text)

    fun configure(dragAndDropSumItem: DragAndDropSumItem) {
        val text = "Current sum: ${dragAndDropSumItem.curSum}"
        this.textView.text = text
    }
}