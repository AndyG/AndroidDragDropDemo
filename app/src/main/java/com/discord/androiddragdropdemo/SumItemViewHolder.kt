package com.discord.androiddragdropdemo

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SumItemViewHolder(view: View) : RecyclerView.ViewHolder(view),
    DragAndDropTouchCallback.DraggableViewHolder {

        override fun onDragStateChanged(dragging: Boolean) {
            // no op
        }

        override fun canDrag(): Boolean {
            return false
        }

    private val textView: TextView = view.findViewById(R.id.draggable_view_text)

    fun configure(dragAndDropSumItem: DragAndDropSumItem) {
        val text = "${dragAndDropSumItem.curSum}"
        this.textView.text = text

        if (dragAndDropSumItem.isTargeted) {
            itemView.setBackgroundColor(Color.LTGRAY)
        } else {
            itemView.setBackgroundColor(Color.WHITE)
        }
    }
}
