package com.discord.androiddragdropdemo.linear

import android.content.ClipData
import android.content.ClipDescription
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.discord.androiddragdropdemo.R

class LinearActivity : AppCompatActivity() {

    private lateinit var redCircle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linear)
        bindViews()
        configureDragAndDrop()
    }

    private fun configureDragAndDrop() {
        redCircle.setOnLongClickListener { view ->
            val item = ClipData.Item(redCircle.text)
            val dragData = ClipData(redCircle.text, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
            val shadow = View.DragShadowBuilder(view)

            view.startDrag(dragData, shadow, null, 0)
        }
    }

    private fun bindViews() {
        redCircle = findViewById(R.id.red_circle)
    }
}
