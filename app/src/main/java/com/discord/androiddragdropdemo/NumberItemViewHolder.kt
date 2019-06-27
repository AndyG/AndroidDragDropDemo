package com.discord.androiddragdropdemo

import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NumberItemViewHolder(view: View, private val layoutManager: LinearLayoutManager)
    : RecyclerView.ViewHolder(view), DragAndDropTouchCallback.DraggableViewHolder {

    private val transformedBoundingBoxRect = Rect()

    private var animation: Animation? = null
    private var didShrinkSinceLastConfigure: Boolean = false

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
        animation?.cancel()
        animation = null
        didShrinkSinceLastConfigure = false

        val text = "Item number: ${dragAndDropNumberItem.number}"
        this.textView.text = text
    }

    fun onHoveredOver(yDragPos: Int) {
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

        val isInMiddle = yDragPos in middleRange
        Log.d("findme", "isInMiddle: $isInMiddle")
        if (isInMiddle) {
            textView.text = "drrrrrrrrrrrrrr"
        } else {
            val text = "Item number: ${data?.number}"
            this.textView.text = text
        }
    }

    private fun computeBoundingBox() {
        layoutManager.getTransformedBoundingBox(this.itemView, false, transformedBoundingBoxRect)
    }

    fun shouldSwap(isMovingUp: Boolean, curY: Int): Boolean {
        computeBoundingBox()
        val centerY = transformedBoundingBoxRect.centerY()
        if (isMovingUp && curY < centerY) {
            // moving up and we've moved above the center
            return true
        } else if (!isMovingUp && curY > centerY) {
            // moving down and we've moved below the center
            return true
        }

        return false
    }

    fun getCenterY(): Int {
        computeBoundingBox()
        return transformedBoundingBoxRect.centerY()
    }

    companion object {
        private const val middlePercentage = 0.25
    }
}
