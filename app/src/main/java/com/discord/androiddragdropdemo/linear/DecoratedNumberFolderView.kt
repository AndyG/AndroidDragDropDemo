package com.discord.androiddragdropdemo.linear

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.marginTop
import com.discord.androiddragdropdemo.R

class DecoratedNumberFolderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.decorated_number_folder_view, this)
    }

    private val highlight: View = findViewById(R.id.number_folder_highlight)

    fun setIsHighlighted(isHighlighted: Boolean) {
        if (isHighlighted) {
            highlight.visibility = View.VISIBLE
        } else {
            highlight.visibility = View.GONE
        }
    }
}