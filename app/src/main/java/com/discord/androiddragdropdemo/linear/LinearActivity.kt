package com.discord.androiddragdropdemo.linear

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.discord.androiddragdropdemo.R
import com.discord.androiddragdropdemo.utils.dpToPx

class LinearActivity : AppCompatActivity() {

    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout

    private var itemSize: Float = 0f
    private var halfItemSize: Float = 0f
    private var addThreshold: Float = 0f

    private var lastScrollTime: Long = 0L

    private var currentTarget: ColoredNumberView? = null

    private var dataSnapshot = ArrayList<Item>()

    private var itemIndex = HashMap<Long, Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linear)
        bindViews()

        val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
        val margin = dpToPx(NUMBER_VIEW_MARGIN_DP, resources).toInt()
        itemSize = numberSize.toFloat() + (margin * 2).toFloat()
        halfItemSize = itemSize / 2
        addThreshold = (itemSize * DISTANCE_FROM_CENTER_FOR_ADD).toFloat()

        val data = generateData(5)
        onNewData(data)
    }

    @SuppressLint("UseSparseArrays")
    private fun onNewData(newData: List<Item>) {
        itemIndex.clear()
        newData.associateByTo(itemIndex, { it.id })

        val oldData = this.dataSnapshot
        Log.d("findme", "oldData: \n${oldData.joinToString("\n")}")
        Log.d("findme", "newData: \n${newData.joinToString("\n")}")

        val cb = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val result = oldData[oldItemPosition].id == newData[newItemPosition].id
//                Log.d("findme", "areItemsTheSame: $oldItemPosition -- $newItemPosition -- $result")
                return result
            }

            override fun getOldListSize(): Int {
                return oldData.size
            }

            override fun getNewListSize(): Int {
                return newData.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val result = oldData[oldItemPosition] == newData[newItemPosition]
//                Log.d("findme", "areContentsTheSame: $oldItemPosition -- $newItemPosition -- $result")
                return result
            }
        }

        val result = DiffUtil.calculateDiff(cb, false)

        result.dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onChanged(position: Int, count: Int, payload: Any?) {
                for (i in 0 until count) {
                    val itemId = dataSnapshot[position + i].id
                    dataSnapshot[position + i] = itemIndex[itemId]!!
                    updateView(position + i)
                }

                Log.d("findme", "updated: \n${dataSnapshot.joinToString("\n")}")
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                throw IllegalStateException("moves are unsupported")
            }

            override fun onInserted(position: Int, count: Int) {
                Log.d("findme", "inserted. position: $position -- count: $count")
                for (i in 0 until count) {
                    val index = position + i
                    val item = newData[index]
                    dataSnapshot.add(index, item)

                    val view = createView(index)
                    view.setOnClickListener { onItemClicked(item.id) }
                    linearLayout.addView(view, index)
                }

                Log.d("findme", "updated: \n${dataSnapshot.joinToString("\n")}")
            }

            override fun onRemoved(position: Int, count: Int) {
                Log.d("findme", "removed. position: $position -- count: $count")
                for (i in 0 until count) {
                    dataSnapshot.removeAt(position)
                    linearLayout.removeViewAt(position)
                }

                Log.d("findme", "updated: \n${dataSnapshot.joinToString("\n")}")
            }
        })

        Log.d("findme", "postOp data: \n${dataSnapshot.joinToString("\n")}")
        if (newData != dataSnapshot) {
            throw IllegalStateException("new data and postop data are not equal!")
        }
    }

    private fun updateView(position: Int) {
        val item = dataSnapshot[position]
        Log.d("findme", "updateView: $position -- item: $item")

        when (item) {
            Item.Placeholder -> {

            }
            is Item.Folder -> {
                val view = linearLayout.getChildAt(position) as NumberFolderView
                if (item.isGone) {
                    view.visibility = View.GONE
                } else {
                    view.visibility = View.VISIBLE
                }
            }
            is Item.ColoredNumber -> {
                val view = linearLayout.getChildAt(position) as ColoredNumberView
                if (item.isGone) {
                    view.visibility = View.GONE
                } else {
                    view.visibility = View.VISIBLE
                }
            }
        }

    }

    private fun createView(index: Int): View {
        when (val item = dataSnapshot[index]) {
            is Item.ColoredNumber -> {
                val view = ColoredNumberView(context = this)
                view.configure(item)
                val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
                val layoutParams = LinearLayout.LayoutParams(numberSize, numberSize)
                layoutParams.setMargins(dpToPx(NUMBER_VIEW_MARGIN_DP, resources).toInt())
                view.layoutParams = layoutParams
                return view
            }
            is Item.Folder -> {
                val view = NumberFolderView(context = this)
                val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
                val marginSize = dpToPx(NUMBER_VIEW_MARGIN_DP, resources)
                val layoutParams = LinearLayout.LayoutParams(numberSize, numberSize)
                layoutParams.setMargins(marginSize.toInt())
                view.layoutParams = layoutParams
                view.setNumChildren(3, itemSize, marginSize)
                return view
            }
            is Item.Placeholder -> {
                return LayoutInflater.from(linearLayout.context).inflate(R.layout.placeholder, linearLayout, false)
            }
        }
    }

    private var draggedView: View? = null

    private fun configureDragAndDrop() {
//        for (i in 0 until linearLayout.childCount) {
//            val view = linearLayout.getChildAt(i) as? ColoredNumberView ?: continue
//
//            view.setOnLongClickListener {
//                val numberStr = view.getColoredNumber()!!.number.toString()
//                val item = ClipData.Item(numberStr)
//                val dragData = ClipData(numberStr, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
//                val shadow = View.DragShadowBuilder(view)
//
//                view.visibility = View.GONE
//                val curIndex = linearLayout.indexOfChild(view)
//                linearLayout.addView(placeholderView, curIndex)
//                Log.d("findme", "initialized placeholder at: ${getPlaceholderIndex()}")
//                draggedView = view
//                view.startDrag(dragData, shadow, null, 0)
//
//                true
//            }
//        }
//
//        linearLayout.setOnDragListener { v, event ->
//            if (event.action == DragEvent.ACTION_DRAG_LOCATION) {
//                val existingPlaceholderViewIndex = getPlaceholderIndex()!!
//                val ghostViewIndex = linearLayout.indexOfChild(draggedView!!)
//
//                val touchY = event.y
//                val numCircles = linearLayout.childCount - 1
//
//                val targetPlaceholderVisualIndex: Int = (0 until numCircles).sortedBy { index ->
//                    val center = itemSize * index + halfItemSize
//                    Math.abs(center - touchY)
//                }.first()
//
//                val isGhostViewAffectingTargetIndex = ghostViewIndex < targetPlaceholderVisualIndex
//
//                val centerOfTarget = itemSize * targetPlaceholderVisualIndex + halfItemSize
//                val isCloseToCenter = Math.abs(centerOfTarget - touchY) < addThreshold
//                val isAboveCenterThreshold = !isCloseToCenter && touchY < centerOfTarget
//                val isBelowCenterThreshold = !isCloseToCenter && touchY > centerOfTarget
//
//                val targetPlaceholderViewIndex = when {
//                    isGhostViewAffectingTargetIndex -> targetPlaceholderVisualIndex + 1 // account for the GONE view.
//                    else -> targetPlaceholderVisualIndex
//                }
//                Log.d("findme", "computed new target placeholder view index: $targetPlaceholderViewIndex")
//
//                val moveDir = when {
//                    existingPlaceholderViewIndex > targetPlaceholderViewIndex -> -1
//                    existingPlaceholderViewIndex < targetPlaceholderViewIndex -> 1
//                    else -> 0
//                }
//
//                // add the placeholder
//                if ((moveDir == 1 && isBelowCenterThreshold) || (moveDir == -1 && isAboveCenterThreshold)) {
//                    // need to move the placeholder.
//                    linearLayout.removeViewAt(existingPlaceholderViewIndex)
//                    linearLayout.addView(placeholderView, targetPlaceholderViewIndex)
//                    Log.d("findme", "new placeholder index: ${getPlaceholderIndex()}")
//                    currentTarget?.setIsHighlighted(false)
//                    currentTarget = null
//                } else if (isCloseToCenter) {
//                    val newTarget = getViewAtVisualIndex(targetPlaceholderVisualIndex, ghostViewIndex) as? ColoredNumberView
//                    if (newTarget !== currentTarget) {
//                        currentTarget?.setIsHighlighted(false)
//                        currentTarget = newTarget
//                        currentTarget?.setIsHighlighted(true)
//                    }
//                }
//
//                val allowScrolls = (System.currentTimeMillis() - lastScrollTime) > SCROLL_THRESHOLD_MS
//                if (allowScrolls) {
//                    val scrollY = scrollView.scrollY
//                    val bottomOfScrollView = scrollY + scrollView.height
//                    val placeholderTop = targetPlaceholderVisualIndex * itemSize
//                    val placeholderBottom = targetPlaceholderVisualIndex * itemSize + itemSize
//
//                    if (placeholderBottom > bottomOfScrollView || Math.abs(touchY - bottomOfScrollView) < (itemSize / 2)) {
//                        scrollView.smoothScrollBy(0, itemSize.toInt())
//                        lastScrollTime = System.currentTimeMillis()
//                    } else if (placeholderTop < scrollY || Math.abs(touchY - scrollY) < (itemSize / 2)) {
//                        scrollView.smoothScrollBy(0, -itemSize.toInt())
//                        lastScrollTime = System.currentTimeMillis()
//                    }
//                }
//            } else if (event.action == DragEvent.ACTION_DRAG_ENDED) {
//                val placeholderIndex = getPlaceholderIndex() ?: throw IllegalStateException("drop with no placeholder")
//                val draggedItemIndex = linearLayout.indexOfChild(draggedView)
//
//                val currentTarget = currentTarget
//
//                if (currentTarget == null) {
//                    linearLayout.removeView(placeholderView)
//                    linearLayout.removeView(draggedView)
//
//                    val adjustedDropIndex =
//                        if (draggedItemIndex < placeholderIndex) placeholderIndex - 1 else placeholderIndex
//                    linearLayout.addView(draggedView, adjustedDropIndex)
//
//                    draggedView?.visibility = View.VISIBLE
//                    draggedView = null
//                } else {
//                    currentTarget.setIsHighlighted(false)
//                    val draggedNumber = (linearLayout.getChildAt(draggedItemIndex) as? ColoredNumberView)?.getColoredNumber()!!
//                    val targetNumber = (currentTarget as? ColoredNumberView)?.getColoredNumber()!!
//                    currentTarget.configure(targetNumber.copy(number = targetNumber.number + draggedNumber.number))
//                    linearLayout.removeView(placeholderView)
//                    linearLayout.removeView(draggedView)
//                    this.currentTarget = null
//                }
//
//                draggedView = null
//            }
//            true
//        }
    }

    private fun bindViews() {
        scrollView = findViewById(R.id.scroll_view)
        linearLayout = findViewById(R.id.linear_layout)
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

    private fun onItemClicked(itemId: Long) {
        if (itemId == -1L) {
            // the placeholder was clicked
            return
        }

        var itemPosition = -1
        var existingPlaceholderPosition = -1

        val newData = ArrayList(dataSnapshot)
        newData.forEachIndexed { index, item ->
            if (item is Item.Folder) {
                newData[index] = item.copy(isGone = false)
            } else if (item is Item.ColoredNumber) {
                newData[index] = item.copy(isGone = false)
            }

            if (item.id == itemId) {
                itemPosition = index
            }

            if (item.id == -1L) {
                existingPlaceholderPosition = index
            }
        }

        val item = newData[itemPosition]

        newData[itemPosition] = when (item) {
            Item.Placeholder -> throw IllegalStateException("what?")
            is Item.Folder -> item.copy(isGone = true)
            is Item.ColoredNumber -> item.copy(isGone = true)
        }

        var insertionIndex = itemPosition
        // remove any old placeholders.
        if (existingPlaceholderPosition != -1) {
            newData.removeAt(existingPlaceholderPosition)
            if (existingPlaceholderPosition < itemPosition) {
                insertionIndex--
            }
        }

        newData.add(insertionIndex, Item.Placeholder)
        onNewData(newData)
    }

    private fun generateData(count: Int): List<Item> {
        return (0..count).map {
            if (false && it % 10 == 0) {
                Item.Folder(isOpen = true, numChildren = 3, isGone = false, id = it.toLong())
            } else {
                Item.ColoredNumber(
                    number = it,
                    color = when (it % 3) {
                        0 -> Item.ColoredNumber.Color.RED
                        1 -> Item.ColoredNumber.Color.GREEN
                        2 -> Item.ColoredNumber.Color.BLUE
                        else -> throw IllegalStateException("unexpected color")
                    },
                    id = it.toLong(),
                    isGone = false
                )
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
