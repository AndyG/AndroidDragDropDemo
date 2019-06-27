package com.discord.androiddragdropdemo

import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SumItemViewHolder(view: View, private val layoutManager: LinearLayoutManager) : RecyclerView.ViewHolder(view),
    DragAndDropTouchCallback.DraggableViewHolder {

    private val transformedBoundingBoxRect = Rect()

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

        val isInMiddle = curY in middleRange

        return if (isInMiddle) {
            DragDropAdapter.DragAndDropOperation.AddToSum
        } else if (isMovingUp && curY in topRange) {
            DragDropAdapter.DragAndDropOperation.Move
        } else if (!isMovingUp && curY in bottomRange) {
            DragDropAdapter.DragAndDropOperation.Move
        } else {
            null
        }
    }

    private fun computeBoundingBox() {
        layoutManager.getTransformedBoundingBox(this.itemView, false, transformedBoundingBoxRect)
    }

    companion object {
        private const val middlePercentage = 0.25
    }
}
