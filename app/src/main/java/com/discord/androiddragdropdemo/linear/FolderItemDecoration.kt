package com.discord.androiddragdropdemo.linear

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView

class FolderItemDecoration(private val drawable: Drawable) : RecyclerView.ItemDecoration() {
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            val viewHolder = parent.getChildViewHolder(view)
            val numChildren: Int
            if (viewHolder is RecyclerActivity.Adapter.FolderViewHolder) {
                numChildren = viewHolder.getNumChildren()
                val params = view.layoutParams as RecyclerView.LayoutParams
                val top = view.top - params.topMargin
                val bottom = view.bottom + params.bottomMargin
                drawable.setBounds(view.left, view.top, view.right, view.bottom)
                drawable.draw(c)
            }
        }
    }
}