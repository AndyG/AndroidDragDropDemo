package com.discord.androiddragdropdemo.linear

import android.content.ClipData
import android.content.ClipDescription
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.discord.androiddragdropdemo.R
import com.discord.androiddragdropdemo.utils.dpToPx
import kotlin.IllegalStateException

class LinearActivity : AppCompatActivity() {

    private lateinit var linearLayout: LinearLayout
    private lateinit var redCircle: TextView
    private lateinit var blueCircle: TextView
    private lateinit var greenCircle: TextView
    private lateinit var placeholderView: View

    private var itemSize: Float = 0f
    private var halfItemSize: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linear)
        bindViews()
        linearLayout.removeView(placeholderView)
        configureDragAndDrop()

        itemSize = dpToPx(128 + 16, resources)
        halfItemSize = itemSize / 2
    }

    private var draggedView: View? = null

    private fun configureDragAndDrop() {
        redCircle.setOnLongClickListener { view ->
            val item = ClipData.Item(redCircle.text)
            val dragData = ClipData(redCircle.text, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
            val shadow = View.DragShadowBuilder(view)

            view.visibility = View.GONE
            val curIndex = linearLayout.indexOfChild(redCircle)
            linearLayout.addView(placeholderView, curIndex)
            draggedView = view
            view.startDrag(dragData, shadow, null, 0)

            true
        }

        blueCircle.setOnLongClickListener { view ->
            val item = ClipData.Item(blueCircle.text)
            val dragData = ClipData(blueCircle.text, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
            val shadow = View.DragShadowBuilder(view)

            view.visibility = View.GONE
            val curIndex = linearLayout.indexOfChild(blueCircle)
            linearLayout.addView(placeholderView, curIndex)
            draggedView = view
            view.startDrag(dragData, shadow, null, 0)

            true
        }

        greenCircle.setOnLongClickListener { view ->
            val item = ClipData.Item(greenCircle.text)
            val dragData = ClipData(greenCircle.text, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
            val shadow = View.DragShadowBuilder(view)

            view.visibility = View.GONE
            val curIndex = linearLayout.indexOfChild(greenCircle)
            linearLayout.addView(placeholderView, curIndex)
            draggedView = view
            view.startDrag(dragData, shadow, null, 0)

            true
        }

        linearLayout.setOnDragListener { v, event ->
            if (event.action == DragEvent.ACTION_DRAG_ENDED) {
                draggedView = null
            } else if (event.action == DragEvent.ACTION_DRAG_LOCATION) {
                val existingPlaceholderIndex = getPlaceholderIndex()
                val draggedItemViewIndex = linearLayout.indexOfChild(draggedView)
                val adjustedDraggedItemIndex = when {
                    existingPlaceholderIndex != null && existingPlaceholderIndex < draggedItemViewIndex -> draggedItemViewIndex - 1
                    else -> draggedItemViewIndex
                }

                val y = event.y

                // this doesn't take into account placeholders.
                val targetIndex: Int = (0..2).sortedBy { index ->
                    val center = itemSize * index + halfItemSize
                    Math.abs(center - y)
                }.first()

                val isDownwardMove = targetIndex > adjustedDraggedItemIndex

                Log.v("findme", "linearlayout got DRAG_LOCATION event. y: ${event.y}. itemSize: $itemSize. computed index: $targetIndex")

                val targetPlaceholderViewIndex = when {
                    isDownwardMove -> targetIndex + 1 // the GONE view is affecting the computation, so add 1 to account for it.
                    else -> targetIndex
                }

                // add the placeholder
                if (existingPlaceholderIndex == null) {
                    linearLayout.addView(placeholderView, targetPlaceholderViewIndex)
                } else if (targetPlaceholderViewIndex != existingPlaceholderIndex){
                    // need to move the placeholder.
                    linearLayout.removeViewAt(existingPlaceholderIndex)
                    linearLayout.addView(placeholderView, targetPlaceholderViewIndex)
                }
            } else if (event.action == DragEvent.ACTION_DROP) {
                val placeholderIndex = getPlaceholderIndex() ?: throw IllegalStateException("drop with no placeholder")
                val draggedItemIndex = linearLayout.indexOfChild(draggedView)

                linearLayout.removeView(placeholderView)
                linearLayout.removeView(draggedView)

                val adjustedDropIndex = if (draggedItemIndex < placeholderIndex) placeholderIndex - 1 else placeholderIndex
                linearLayout.addView(draggedView, adjustedDropIndex)

                draggedView?.visibility = View.VISIBLE
            }
            true
        }
    }

    private fun bindViews() {
        linearLayout = findViewById(R.id.linear_layout)
        redCircle = findViewById(R.id.red_circle)
        blueCircle = findViewById(R.id.blue_circle)
        greenCircle = findViewById(R.id.green_circle)
        placeholderView = findViewById(R.id.placeholder_view)
        placeholderView.tag = TAG_PLACEHOLDER
    }

    private fun getPlaceholderIndex(): Int? {
        for (i in 0..linearLayout.childCount) {
            if (linearLayout.getChildAt(i).tag == TAG_PLACEHOLDER) {
                return i
            }
        }

        return null
    }

    companion object {
        private const val TAG_PLACEHOLDER = "placeholder"
    }
}
