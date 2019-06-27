package com.discord.androiddragdropdemo

import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NumberItemViewHolder(view: View)
    : RecyclerView.ViewHolder(view), DragAndDropTouchCallback.DraggableViewHolder {

    private var animation: Animation? = null
    private var didShrinkSinceLastConfigure: Boolean = false

    override fun onDragStateChanged(dragging: Boolean) {
        // no op
    }

    override fun canDrag(): Boolean {
        return true
    }

    private val textView: TextView = view.findViewById(R.id.draggable_view_text)

    fun configure(dragAndDropNumberItem: DragAndDropNumberItem) {
        this.itemView.visibility = View.VISIBLE
        animation?.cancel()
        animation = null
        didShrinkSinceLastConfigure = false

        val text = "Item number: ${dragAndDropNumberItem.number}"
        this.textView.text = text
    }

    fun onDroppedOverSum() {
        Log.d("findme", "onDroppedOverSum")
        this.itemView.visibility = View.GONE
    }
}
