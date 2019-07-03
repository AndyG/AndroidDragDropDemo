package com.discord.androiddragdropdemo.linear

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.discord.androiddragdropdemo.R

class NumberFolderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.number_folder_view, this)
        orientation = VERTICAL
    }

    private val backgroundView: View = findViewById(R.id.number_folder_background)

    private var hasMultipleChildren = false
    fun setNumChildren(numChildren: Int) {
        val backgroundLps = backgroundView.layoutParams
        backgroundLps.height = (backgroundLps.width * numChildren).toInt()
        backgroundView.layoutParams = backgroundLps

        if (numChildren == 1 && hasMultipleChildren) {
            backgroundView.setBackgroundResource(R.drawable.circle_gray)
            this.hasMultipleChildren = false
        } else if (numChildren > 1 && !hasMultipleChildren) {
            backgroundView.setBackgroundResource(R.drawable.rounded_rectangle_gray)
            this.hasMultipleChildren = true
        }

        backgroundView.invalidate()
    }
}