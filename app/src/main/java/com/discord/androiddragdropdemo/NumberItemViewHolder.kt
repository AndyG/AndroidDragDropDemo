package com.discord.androiddragdropdemo

import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NumberItemViewHolder(private val view: View)
    : RecyclerView.ViewHolder(view), DragAndDropTouchCallback.DraggableViewHolder {

    private var animation: Animation? = null
    private var didShrinkSinceLastConfigure: Boolean = false

    override fun onDragStateChanged(dragging: Boolean) {
        // no op
    }

    override fun canDrag(): Boolean {
        return true
    }

    val textView: TextView = view.findViewById(R.id.draggable_view_text)
    val divider: View = view.findViewById(R.id.draggable_view_divider)

    fun configure(dragAndDropNumberItem: DragAndDropNumberItem) {
        animation?.cancel()
        animation = null
        didShrinkSinceLastConfigure = false

        val text = "Item number: ${dragAndDropNumberItem.number}"
        this.textView.text = text

        this.textView.visibility = View.VISIBLE
        divider.visibility = View.VISIBLE
    }

    fun stopRenderingContent(isUp: Boolean) {
        this.textView.text = "Adding to sum..."
        shrink(isUp)
//        this.textView.visibility = View.GONE
//        divider.visibility = View.GONE
    }

    private fun shrink(isUp: Boolean) {
        if (this.didShrinkSinceLastConfigure) {
            return
        }

        this.didShrinkSinceLastConfigure = true

        Log.d("findme", "shrink: $isUp")

        val animId = when {
            isUp -> R.anim.shrink_up
            else -> R.anim.shrink_down
        }

        val anim = AnimationUtils.loadAnimation(textView.context, animId)

        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) { }
            override fun onAnimationStart(animation: Animation?) { }

            override fun onAnimationEnd(animation: Animation?) {
                textView.visibility = View.GONE
                this@NumberItemViewHolder.animation = null
            }
        })

        this.animation = anim
        textView.startAnimation(anim)
    }
}
