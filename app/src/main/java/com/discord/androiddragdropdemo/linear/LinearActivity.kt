package com.discord.androiddragdropdemo.linear

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.discord.androiddragdropdemo.R
import com.discord.androiddragdropdemo.utils.dpToPx

class LinearActivity : AppCompatActivity() {

    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout
    private lateinit var placeholderView: View

    private var itemSize: Float = 0f
    private var halfItemSize: Float = 0f

    private var lastScrollTime: Long = 0L

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
        for (i in 0 until linearLayout.childCount) {
            val view = linearLayout.getChildAt(i) as TextView

            view.setOnLongClickListener {
                val item = ClipData.Item(view.text)
                val dragData = ClipData(view.text, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                val shadow = View.DragShadowBuilder(view)

                view.visibility = View.GONE
                val curIndex = linearLayout.indexOfChild(view)
                linearLayout.addView(placeholderView, curIndex)
                draggedView = view
                view.startDrag(dragData, shadow, null, 0)

                true
            }
        }

        linearLayout.setOnDragListener { v, event ->
            if (event.action == DragEvent.ACTION_DRAG_LOCATION) {
                val existingPlaceholderIndex = getPlaceholderIndex()
                val draggedItemViewIndex = linearLayout.indexOfChild(draggedView)
                val adjustedDraggedItemIndex = when {
                    existingPlaceholderIndex != null && existingPlaceholderIndex < draggedItemViewIndex -> draggedItemViewIndex - 1
                    else -> draggedItemViewIndex
                }

                val y = event.y

                val numCircles = when {
                    existingPlaceholderIndex != null -> linearLayout.childCount - 1
                    else -> linearLayout.childCount
                }

                val targetIndex: Int = (0 until numCircles).sortedBy { index ->
                    val center = itemSize * index + halfItemSize
                    Math.abs(center - y)
                }.first()

                val isDownwardMove = targetIndex > adjustedDraggedItemIndex

                val targetPlaceholderViewIndex = when {
                    isDownwardMove -> targetIndex + 1 // the GONE view is affecting the computation, so add 1 to account for it.
                    else -> targetIndex
                }

                // add the placeholder
                if (existingPlaceholderIndex == null) {
                    linearLayout.addView(placeholderView, targetPlaceholderViewIndex)
                } else if (targetPlaceholderViewIndex != existingPlaceholderIndex) {
                    // need to move the placeholder.
                    linearLayout.removeViewAt(existingPlaceholderIndex)
                    linearLayout.addView(placeholderView, targetPlaceholderViewIndex)
                }

                val allowScrolls = (System.currentTimeMillis() - lastScrollTime) > SCROLL_THRESHOLD_MS
                if (allowScrolls) {
                    val touchY = y
                    val scrollY = scrollView.scrollY
                    val bottomOfScrollView = scrollY + scrollView.height
                    val placeholderTop = targetIndex * itemSize
                    val placeholderBottom = targetIndex * itemSize + itemSize

                    Log.d("findme", "bottom: $bottomOfScrollView -- touchPos: $touchY -- itemSize: $itemSize")
                    Log.d("findme", "touchY: $touchY -- scrollY: $scrollY")
                    Log.d("findme", "placeholder top: $placeholderTop")
                    Log.d("findme", "placeholder bottom: $placeholderBottom")
                    Log.d("findme", "--------------------------------")

                    if (placeholderBottom > bottomOfScrollView || Math.abs(touchY - bottomOfScrollView) < (itemSize / 2)) {
                        scrollView.smoothScrollBy(0, itemSize.toInt())
                        lastScrollTime = System.currentTimeMillis()
                    } else if (placeholderTop < scrollY || Math.abs(touchY - scrollY) < (itemSize / 2)) {
                        scrollView.smoothScrollBy(0, -itemSize.toInt())
                        lastScrollTime = System.currentTimeMillis()
                    }
                }
            } else if (event.action == DragEvent.ACTION_DRAG_ENDED) {
                val placeholderIndex = getPlaceholderIndex() ?: throw IllegalStateException("drop with no placeholder")
                val draggedItemIndex = linearLayout.indexOfChild(draggedView)

                linearLayout.removeView(placeholderView)
                linearLayout.removeView(draggedView)

                val adjustedDropIndex = if (draggedItemIndex < placeholderIndex) placeholderIndex - 1 else placeholderIndex
                linearLayout.addView(draggedView, adjustedDropIndex)

                draggedView?.visibility = View.VISIBLE
                draggedView = null
            }
            true
        }
    }

    private fun bindViews() {
        scrollView = findViewById(R.id.scroll_view)
        linearLayout = findViewById(R.id.linear_layout)
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
        private const val SCROLL_THRESHOLD_MS = 250L
    }
}
