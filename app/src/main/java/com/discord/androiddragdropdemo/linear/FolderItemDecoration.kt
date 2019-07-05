package com.discord.androiddragdropdemo.linear

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView

class FolderItemDecoration(
    private val drawableNoChildren: Drawable,
    private val drawableWithChildren: Drawable
) : RecyclerView.ItemDecoration() {
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            val viewHolder = parent.getChildViewHolder(view)
            if (viewHolder is RecyclerActivity.Adapter.FolderViewHolder) {
                val numChildren = viewHolder.getNumChildren()
                if (numChildren == 0) {
                    drawableNoChildren.setBounds(view.left, view.top, view.right, view.bottom)
                    drawableNoChildren.draw(c)
                } else {
                    val viewHeight = view.height
                    val viewMargin = view.marginTop
                    val totalViewHeight = viewHeight + viewMargin
                    val drawableHeight = totalViewHeight * (1 + numChildren)
                    drawableWithChildren.setBounds(view.left, view.top, view.right, view.top + drawableHeight)
                    drawableWithChildren.draw(c)
                }
            }
        }
    }
}