package com.discord.androiddragdropdemo

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SumItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view),
    DragAndDropTouchCallback.DraggableViewHolder {

        override fun onDragStateChanged(dragging: Boolean) {
            // no op
        }

        override fun canDrag(): Boolean {
            return false
        }

    val textView: TextView = view.findViewById(R.id.draggable_view_text)

    fun configure(dragAndDropSumItem: DragAndDropSumItem) {
        val text = "Current sum: ${dragAndDropSumItem.curSum}"
        this.textView.text = text

        if (dragAndDropSumItem.isTargeted) {
            view.setBackgroundColor(Color.LTGRAY)
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
