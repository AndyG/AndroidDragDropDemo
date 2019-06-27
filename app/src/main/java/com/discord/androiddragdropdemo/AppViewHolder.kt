package com.discord.androiddragdropdemo

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    private val textView: TextView = view.findViewById(R.id.draggable_view_text)

    fun configure(dragAndDropItem: DragAndDropItem) {
        val text = "Item number: ${dragAndDropItem.id}"
        this.textView.text = text
    }
}
