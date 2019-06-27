package com.discord.androiddragdropdemo

import android.graphics.Rect
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NumberItemViewHolder(view: View, private val layoutManager: LinearLayoutManager)
    : RecyclerView.ViewHolder(view), DragAndDropTouchCallback.DraggableViewHolder {

    private val transformedBoundingBoxRect = Rect()

    var data: DragAndDropNumberItem? = null

    override fun onDragStateChanged(dragging: Boolean) {
        // no op
    }

    override fun canDrag(): Boolean {
        return true
    }

    private val textView: TextView = view.findViewById(R.id.draggable_view_text)

    fun configure(dragAndDropNumberItem: DragAndDropNumberItem) {
        this.data = dragAndDropNumberItem

        this.itemView.visibility = View.VISIBLE

        val text = "Item number: ${dragAndDropNumberItem.number}"
        this.textView.text = text
    }

    private fun computeBoundingBox() {
        layoutManager.getTransformedBoundingBox(this.itemView, false, transformedBoundingBoxRect)
    }

    fun getDragAndDropOperation(isMovingUp: Boolean, curY: Int): DragDropAdapter.DragAndDropOperation? {
        computeBoundingBox()
        val height = transformedBoundingBoxRect.height()

        val nonMiddlePercent = 1 - middlePercentage
        val halfNonMiddlePercent = nonMiddlePercent / 2

        val outerSize = height * halfNonMiddlePercent

        val topOfTopRange = (transformedBoundingBoxRect.top)
        val bottomOfTopRange = (topOfTopRange + outerSize).toInt()
        val topRange = topOfTopRange..bottomOfTopRange

        val bottomOfBottomRange = transformedBoundingBoxRect.bottom
        val topOfBottomRange = (bottomOfBottomRange - outerSize).toInt()
        val bottomRange = topOfBottomRange..bottomOfBottomRange

        val middleRange = bottomOfTopRange..topOfBottomRange

        Log.d("findme", "top range: $topRange")
        Log.d("findme", "middle range: $middleRange")
        Log.d("findme", "bottom range: $bottomRange")

        val isInMiddle = curY in middleRange

        Log.d("findme", "isInMiddle: $isInMiddle")

        return if (curY in middleRange) {
            DragDropAdapter.DragAndDropOperation.CreateSum
        } else if (isMovingUp && curY in topRange) {
            DragDropAdapter.DragAndDropOperation.Move
        } else if (!isMovingUp && curY in bottomRange) {
            DragDropAdapter.DragAndDropOperation.Move
        } else {
            null
        }
    }

    fun getCenterY(): Int {
        computeBoundingBox()
        return transformedBoundingBoxRect.centerY()
    }

    fun onDroppedOverSum() {
        Log.d("findme", "onDroppedOverSum")
        this.itemView.visibility = View.GONE
    }

    fun configureWithSum(otherNumber: Int?) {
        if (otherNumber != null) {
            val text = "Item number: ${data?.number} + $otherNumber"
            this.textView.text = text
        } else {
            val text = "Item number: ${data?.number}"
            this.textView.text = text
        }
    }

    companion object {
        private const val middlePercentage = 0.25
    }
}
