package com.discord.androiddragdropdemo.linear

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.discord.androiddragdropdemo.R
import com.discord.androiddragdropdemo.utils.dpToPx

class LinearActivity : AppCompatActivity() {

    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout
    private lateinit var placeholderView: View

    private var itemSize: Float = 0f
    private var halfItemSize: Float = 0f
    private var addThreshold: Float = 0f

    private var lastScrollTime: Long = 0L

    private var currentTarget: ColoredNumberView? = null

    private var data: List<Item> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linear)
        bindViews()
        linearLayout.removeView(placeholderView)

        val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
        val margin = dpToPx(NUMBER_VIEW_MARGIN_DP, resources).toInt()
        itemSize = numberSize.toFloat() + (margin * 2).toFloat()
        halfItemSize = itemSize / 2
        addThreshold = (itemSize * DISTANCE_FROM_CENTER_FOR_ADD).toFloat()

        generateData(100)
        configureDragAndDrop()
    }

    private var draggedView: View? = null

    private fun configureDragAndDrop() {
        for (i in 0 until linearLayout.childCount) {
            val view = linearLayout.getChildAt(i) as? ColoredNumberView ?: continue

            view.setOnLongClickListener {
                val numberStr = view.getColoredNumber()!!.number.toString()
                val item = ClipData.Item(numberStr)
                val dragData = ClipData(numberStr, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                val shadow = View.DragShadowBuilder(view)

                view.visibility = View.GONE
                val curIndex = linearLayout.indexOfChild(view)
                linearLayout.addView(placeholderView, curIndex)
                Log.d("findme", "initialized placeholder at: ${getPlaceholderIndex()}")
                draggedView = view
                view.startDrag(dragData, shadow, null, 0)

                true
            }
        }

        linearLayout.setOnDragListener { v, event ->
            if (event.action == DragEvent.ACTION_DRAG_LOCATION) {
                val existingPlaceholderViewIndex = getPlaceholderIndex()!!
                val ghostViewIndex = linearLayout.indexOfChild(draggedView!!)

                val touchY = event.y
                val numCircles = linearLayout.childCount - 1

                val targetPlaceholderVisualIndex: Int = (0 until numCircles).sortedBy { index ->
                    val center = itemSize * index + halfItemSize
                    Math.abs(center - touchY)
                }.first()

                val isGhostViewAffectingTargetIndex = ghostViewIndex < targetPlaceholderVisualIndex

                val centerOfTarget = itemSize * targetPlaceholderVisualIndex + halfItemSize
                val isCloseToCenter = Math.abs(centerOfTarget - touchY) < addThreshold
                val isAboveCenterThreshold = !isCloseToCenter && touchY < centerOfTarget
                val isBelowCenterThreshold = !isCloseToCenter && touchY > centerOfTarget

                val targetPlaceholderViewIndex = when {
                    isGhostViewAffectingTargetIndex -> targetPlaceholderVisualIndex + 1 // account for the GONE view.
                    else -> targetPlaceholderVisualIndex
                }
                Log.d("findme", "computed new target placeholder view index: $targetPlaceholderViewIndex")

                val moveDir = when {
                    existingPlaceholderViewIndex > targetPlaceholderViewIndex -> -1
                    existingPlaceholderViewIndex < targetPlaceholderViewIndex -> 1
                    else -> 0
                }

                // add the placeholder
                if ((moveDir == 1 && isBelowCenterThreshold) || (moveDir == -1 && isAboveCenterThreshold)) {
                    // need to move the placeholder.
                    linearLayout.removeViewAt(existingPlaceholderViewIndex)
                    linearLayout.addView(placeholderView, targetPlaceholderViewIndex)
                    Log.d("findme", "new placeholder index: ${getPlaceholderIndex()}")
                    currentTarget?.setIsHighlighted(false)
                    currentTarget = null
                } else if (isCloseToCenter) {
                    val newTarget = getViewAtVisualIndex(targetPlaceholderVisualIndex, ghostViewIndex) as? ColoredNumberView
                    if (newTarget !== currentTarget) {
                        currentTarget?.setIsHighlighted(false)
                        currentTarget = newTarget
                        currentTarget?.setIsHighlighted(true)
                    }
                }

                val allowScrolls = (System.currentTimeMillis() - lastScrollTime) > SCROLL_THRESHOLD_MS
                if (allowScrolls) {
                    val scrollY = scrollView.scrollY
                    val bottomOfScrollView = scrollY + scrollView.height
                    val placeholderTop = targetPlaceholderVisualIndex * itemSize
                    val placeholderBottom = targetPlaceholderVisualIndex * itemSize + itemSize

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

                val currentTarget = currentTarget

                if (currentTarget == null) {
                    linearLayout.removeView(placeholderView)
                    linearLayout.removeView(draggedView)

                    val adjustedDropIndex =
                        if (draggedItemIndex < placeholderIndex) placeholderIndex - 1 else placeholderIndex
                    linearLayout.addView(draggedView, adjustedDropIndex)

                    draggedView?.visibility = View.VISIBLE
                    draggedView = null
                } else {
                    currentTarget.setIsHighlighted(false)
                    val draggedNumber = (linearLayout.getChildAt(draggedItemIndex) as? ColoredNumberView)?.getColoredNumber()!!
                    val targetNumber = (currentTarget as? ColoredNumberView)?.getColoredNumber()!!
                    currentTarget.configure(targetNumber.copy(number = targetNumber.number + draggedNumber.number))
                    linearLayout.removeView(placeholderView)
                    linearLayout.removeView(draggedView)
                    this.currentTarget = null
                }

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

    private fun getViewAtVisualIndex(visualIndex: Int, ghostViewIndex: Int): View {
        return if (ghostViewIndex <= visualIndex) {
            linearLayout.getChildAt(visualIndex + 1)
        } else {
            linearLayout.getChildAt(visualIndex)
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun generateData(count: Int) {
        val data = (0..count).map {
            if (it % 10 == 0) {
                Item.Folder(isOpen = true, numChildren = 3, id = it.toLong())
            } else {
                Item.ColoredNumber(
                    number = it,
                    color = when (it % 3) {
                        0 -> Item.ColoredNumber.Color.RED
                        1 -> Item.ColoredNumber.Color.GREEN
                        2 -> Item.ColoredNumber.Color.BLUE
                        else -> throw IllegalStateException("unexpected color")
                    },
                    id = it.toLong()
                )
            }
        }

        data.forEach { item ->
            if (item is Item.ColoredNumber) {
                val view = ColoredNumberView(context = this)
                view.configure(item)
                val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
                val layoutParams = LinearLayout.LayoutParams(numberSize, numberSize)
                layoutParams.setMargins(dpToPx(NUMBER_VIEW_MARGIN_DP, resources).toInt())
                view.layoutParams = layoutParams
                linearLayout.addView(view)
            } else if (item is Item.Folder) {
                val view = NumberFolderView(context = this)
                val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
                val marginSize = dpToPx(NUMBER_VIEW_MARGIN_DP, resources)
                val layoutParams = LinearLayout.LayoutParams(numberSize, numberSize)
                layoutParams.setMargins(marginSize.toInt())
                view.layoutParams = layoutParams
                view.setNumChildren(3, itemSize, marginSize)
                linearLayout.addView(view)
            }
        }
    }

    companion object {
        private const val TAG_PLACEHOLDER = "placeholder"
        private const val SCROLL_THRESHOLD_MS = 250L
        private const val DISTANCE_FROM_CENTER_FOR_ADD = 0.2
        private const val NUMBER_VIEW_SIZE_DP = 64
        private const val NUMBER_VIEW_MARGIN_DP = 4
    }
}
