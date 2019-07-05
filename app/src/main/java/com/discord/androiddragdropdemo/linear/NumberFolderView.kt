package com.discord.androiddragdropdemo.linear

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.marginTop
import com.discord.androiddragdropdemo.R

class NumberFolderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.number_folder_view, this)
    }

    private val backgroundView: View = findViewById(R.id.number_folder_background)
    private val highlight: View = findViewById(R.id.number_folder_highlight)

    private var hasChildren = false
    fun setNumChildren(numChildren: Int, itemSize: Float, itemMargin: Float) {
        val backgroundLps = backgroundView.layoutParams
        val newHeight = (itemSize + itemSize * numChildren) - (itemMargin * 2)
        Log.d("findme", "numChildren: $numChildren -- itemSize: $itemSize -- computed height: $newHeight")
        backgroundLps.height = newHeight.toInt()
        backgroundView.layoutParams = backgroundLps

        if (numChildren == 0 && hasChildren) {
            backgroundView.setBackgroundResource(R.drawable.circle_gray)
            this.hasChildren = false
        } else if (numChildren > 0 && !hasChildren) {
            backgroundView.setBackgroundResource(R.drawable.rounded_rectangle_gray)
            this.hasChildren = true
        }
    }

    fun setIsHighlighted(isHighlighted: Boolean) {
        if (isHighlighted) {
            highlight.visibility = View.VISIBLE
        } else {
            highlight.visibility = View.GONE
        }
    }
}