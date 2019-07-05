package com.discord.androiddragdropdemo.linear

import android.animation.LayoutTransition
import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.discord.androiddragdropdemo.R
import com.discord.androiddragdropdemo.repository.ExpandedFolderRepository
import com.discord.androiddragdropdemo.utils.dpToPx
import io.reactivex.disposables.Disposable

class LinearActivity : AppCompatActivity() {

    private lateinit var viewModel: ListViewModel

    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout

    private var itemSize: Float = 0f
    private var halfItemSize: Float = 0f
    private var addThreshold: Float = 0f

    private var lastScrollTime: Long = 0L

    private var dataSnapshot : List<Item> = emptyList()

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linear)
        bindViews()

        val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
        val margin = dpToPx(NUMBER_VIEW_MARGIN_DP, resources).toInt()
        itemSize = numberSize.toFloat() + (margin * 2).toFloat()
        halfItemSize = itemSize / 2
        addThreshold = (itemSize * DISTANCE_FROM_CENTER_FOR_ADD)

        configureDragAndDrop()

        viewModel = ViewModelProviders
            .of(this)
            .get(ListViewModel::class.java)

        disposable = viewModel
            .observeListItems()
            .subscribe(::onNewData)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private fun onNewData(newData: List<Item>) {
        val diffUtilCallback = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return dataSnapshot[oldItemPosition].id == newData[newItemPosition].id
            }

            override fun getOldListSize(): Int {
                return dataSnapshot.size
            }

            override fun getNewListSize(): Int {
                return newData.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return dataSnapshot[oldItemPosition] == newData[newItemPosition]
            }
        }

        // TODO: use a sparse array here for perf.
        val updates = ArrayList<Update?>(dataSnapshot.size)
        for (i in 0 until dataSnapshot.size) {
            updates.add(null)
        }

        val result = DiffUtil.calculateDiff(diffUtilCallback, false)
        this.dataSnapshot = newData

        result.dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onChanged(position: Int, count: Int, payload: Any?) {
                for (i in 0 until count) {
                    updates[position + i] = Update.CHANGE
                }
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                throw IllegalStateException("moves are unsupported for now since we're not using animations anyway.")
            }

            override fun onInserted(position: Int, count: Int) {
                for (i in 0 until count) {
                    updates.add(position, Update.INSERT)
                }
            }

            override fun onRemoved(position: Int, count: Int) {
                for (i in 0 until count) {
                    // this shifts the updates appropriately.
                    updates.removeAt(position)
                    removeView(position)
                }
            }
        })

        updates.forEachIndexed { position, update ->
            when (update) {
                null -> { }
                Update.INSERT -> {
                    insertView(position)
                    updateView(position)
                }
                Update.CHANGE -> updateView(position)
            }
        }
    }

    private fun updateView(position: Int) {
        when (val item = dataSnapshot[position]) {
            is Item.PlaceholderListItem -> {
                val view = linearLayout.getChildAt(position)
                if (item.folderId != null) {
                    view.setBackgroundResource(R.drawable.ring_red)
                } else {
                    view.setBackgroundResource(R.drawable.ring_black)
                }
                if (view is NumberFolderView || view is ColoredNumberView) {
                    throw IllegalStateException("invalid view type: ${view.javaClass}")
                }
            }
            is Item.FolderListItem -> {
                val view = linearLayout.getChildAt(position) as NumberFolderView
                val marginSize = dpToPx(NUMBER_VIEW_MARGIN_DP, resources)
                view.setIsHighlighted(isHighlighted = item.isTargeted)
                view.setNumChildren(item.numChildren, itemSize, marginSize)
                view.setOnClickListener {
                    if (item.isOpen) {
                        ExpandedFolderRepository.collapseFolder(item.id)
                    } else {
                        ExpandedFolderRepository.expandFolder(item.id)
                    }
                }
            }
            is Item.ColoredNumberListItem -> {
                val view = linearLayout.getChildAt(position) as ColoredNumberView
                view.configure(item.coloredNumber)
                view.setIsHighlighted(item.isTargeted)

                view.setOnLongClickListener {
                    if (item.folderId == null) {
                        val numberStr = item.coloredNumber.number.toString()
                        val clipDataItem = ClipData.Item(numberStr)
                        val dragData = ClipData(numberStr, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), clipDataItem)
                        val shadow = View.DragShadowBuilder(view)

                        view.startDrag(dragData, shadow, null, 0)
                        viewModel.onDragStarted(item)
                    }
                    true
                }
            }
        }
    }

    private fun insertView(position: Int) {
        val view = createView(position)
        linearLayout.addView(view, position)
    }

    private fun removeView(position: Int) {
        linearLayout.removeViewAt(position)
    }

    private fun createView(index: Int): View {
        when (dataSnapshot[index]) {
            is Item.ColoredNumberListItem -> {
                val view = ColoredNumberView(context = this)
                val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
                val layoutParams = LinearLayout.LayoutParams(numberSize, numberSize)
                layoutParams.setMargins(dpToPx(NUMBER_VIEW_MARGIN_DP, resources).toInt())
                view.layoutParams = layoutParams

                return view
            }
            is Item.FolderListItem -> {
                val view = NumberFolderView(context = this)
                val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
                val marginSize = dpToPx(NUMBER_VIEW_MARGIN_DP, resources)
                val layoutParams = LinearLayout.LayoutParams(numberSize, numberSize)
                layoutParams.setMargins(marginSize.toInt())
                view.layoutParams = layoutParams
                return view
            }
            is Item.PlaceholderListItem -> {
                return LayoutInflater.from(linearLayout.context).inflate(R.layout.placeholder, linearLayout, false)
            }
        }
    }

    private fun configureDragAndDrop() {
        linearLayout.setOnDragListener { _, event ->
            if (event.action == DragEvent.ACTION_DRAG_STARTED) {
                linearLayout.layoutTransition = null
            } else if (event.action == DragEvent.ACTION_DRAG_LOCATION) {
                val touchY = event.y
                val numCircles = dataSnapshot.size

                val closestHoverTargetIndex: Int = (0 until numCircles).sortedBy { index ->
                    val center = itemSize * index + halfItemSize
                    Math.abs(center - touchY)
                }.first()

                val existingPlaceholderIndex = dataSnapshot.indexOfFirst { it is Item.PlaceholderListItem }
                if (existingPlaceholderIndex < 0) throw IllegalStateException("must have placeholder during drag")

                val considerMove = closestHoverTargetIndex != existingPlaceholderIndex
                if (considerMove) {
                    val centerOfTarget = itemSize * closestHoverTargetIndex + halfItemSize
                    val isCloseToCenter = Math.abs(centerOfTarget - touchY) < addThreshold
                    val isAboveCenterThreshold = !isCloseToCenter && touchY < centerOfTarget
                    val isBelowCenterThreshold = !isCloseToCenter && touchY > centerOfTarget
                    val isDownwardMove = closestHoverTargetIndex > existingPlaceholderIndex

                    if ((isDownwardMove && isBelowCenterThreshold)) {
                        viewModel.targetItem(
                            dataSnapshot[closestHoverTargetIndex],
                            ListViewModel.TargetType.BELOW
                        )
                    } else if ((!isDownwardMove && isAboveCenterThreshold)) {
                        viewModel.targetItem(
                            dataSnapshot[closestHoverTargetIndex],
                            ListViewModel.TargetType.ABOVE
                        )
                    } else if (isCloseToCenter) {
                        viewModel.targetItem(
                            dataSnapshot[closestHoverTargetIndex],
                            ListViewModel.TargetType.INSIDE
                        )
                    }
                } else {
                    viewModel.ensureNoTarget()
                }


                val allowScrolls = (System.currentTimeMillis() - lastScrollTime) > SCROLL_THRESHOLD_MS
                if (allowScrolls) {
                    val scrollY = scrollView.scrollY
                    val bottomOfScrollView = scrollY + scrollView.height
                    val placeholderTop = existingPlaceholderIndex * itemSize
                    val placeholderBottom = existingPlaceholderIndex * itemSize + itemSize

                    if (placeholderBottom > bottomOfScrollView || Math.abs(touchY - bottomOfScrollView) < (itemSize / 2)) {
                        scrollView.smoothScrollBy(0, itemSize.toInt())
                        lastScrollTime = System.currentTimeMillis()
                    } else if (placeholderTop < scrollY || Math.abs(touchY - scrollY) < (itemSize / 2)) {
                        scrollView.smoothScrollBy(0, -itemSize.toInt())
                        lastScrollTime = System.currentTimeMillis()
                    }
                }
            } else if (event.action == DragEvent.ACTION_DRAG_ENDED) {
                viewModel.onDragEnded()
                linearLayout.layoutTransition = LayoutTransition()
            }
            true
        }
    }

    private fun bindViews() {
        scrollView = findViewById(R.id.scroll_view)
        linearLayout = findViewById(R.id.linear_layout)
    }

    companion object {
        private const val SCROLL_THRESHOLD_MS = 250L
        private const val DISTANCE_FROM_CENTER_FOR_ADD = 0.2f
        private const val NUMBER_VIEW_SIZE_DP = 64
        private const val NUMBER_VIEW_MARGIN_DP = 4
    }

    enum class Update {
        CHANGE,
        INSERT
    }
}
