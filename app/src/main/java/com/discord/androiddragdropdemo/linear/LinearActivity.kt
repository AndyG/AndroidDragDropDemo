package com.discord.androiddragdropdemo.linear

import android.content.ClipData
import android.content.ClipDescription
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.discord.androiddragdropdemo.R

class LinearActivity : AppCompatActivity() {

    private lateinit var redCircle: TextView
    private lateinit var blueCircle: TextView

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

            val isDragStarting = view.startDrag(dragData, shadow, null, 0)
            view.setBackgroundResource(R.drawable.ring_black)
            isDragStarting
        }

        redCircle.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_ENDED -> {
                    redCircle.setBackgroundResource(R.drawable.circle_red)
                    redCircle.invalidate()
                    true
                }
                else -> true
            }
        }

        blueCircle.setOnDragListener { view, event ->
            when (val action = event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED -> {
                    blueCircle.text = "!"
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    blueCircle.text = "2"
                    true
                }
                DragEvent.ACTION_DROP -> {
                    blueCircle.text = "3"
                    true
                }
                else -> false
            }
        }
    }

    private fun handleDraggedOnto() {

    }

    private fun bindViews() {
        redCircle = findViewById(R.id.red_circle)
        blueCircle = findViewById(R.id.blue_circle)
    }
}
