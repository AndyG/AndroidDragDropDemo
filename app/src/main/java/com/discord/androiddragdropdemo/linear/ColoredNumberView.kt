package com.discord.androiddragdropdemo.linear

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.discord.androiddragdropdemo.R

class ColoredNumberView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.colored_number_view, this)
    }

    private val textView: TextView = findViewById(R.id.colored_number_text)
    private val highlight: View = findViewById(R.id.colored_number_highlight)

    private var coloredNumber: ColoredNumber? = null

    fun getColoredNumber(): ColoredNumber? {
        return this.coloredNumber
    }

    fun configure(coloredNumber: ColoredNumber) {
        this.coloredNumber = coloredNumber
        this.textView.text = coloredNumber.number.toString()
        setBackgroundResource(when (coloredNumber.color) {
            ColoredNumber.Color.RED -> R.drawable.circle_red
            ColoredNumber.Color.BLUE -> R.drawable.circle_blue
            ColoredNumber.Color.GREEN -> R.drawable.circle_green
        })
    }

    fun setIsHighlighted(isHighlighted: Boolean) {
        if (isHighlighted) {
            highlight.visibility = View.VISIBLE
        } else {
            highlight.visibility = View.GONE
        }
    }
}