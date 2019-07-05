package com.discord.androiddragdropdemo.linear

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.discord.androiddragdropdemo.R
import com.discord.androiddragdropdemo.recycler.DragAndDropTouchCallback
import com.discord.androiddragdropdemo.repository.ExpandedFolderRepository
import com.discord.androiddragdropdemo.utils.dpToPx
import io.reactivex.disposables.Disposable

class RecyclerActivity : AppCompatActivity() {

    private lateinit var viewModel: RecyclerViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: Adapter

    private var itemSize: Float = 0f
    private var halfItemSize: Float = 0f
    private var addThreshold: Float = 0f

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)
        bindViews()

        val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
        val margin = dpToPx(NUMBER_VIEW_MARGIN_DP, resources).toInt()
        itemSize = numberSize.toFloat() + (margin * 2).toFloat()
        halfItemSize = itemSize / 2
        addThreshold = (itemSize * DISTANCE_FROM_CENTER_FOR_ADD)

        configureRecycler()

        viewModel = ViewModelProviders
            .of(this)
            .get(RecyclerViewModel::class.java)

        disposable = viewModel
            .observeListItems()
            .subscribe(::onNewData)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private fun configureRecycler() {
        val layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)
        recyclerAdapter = Adapter(::onOperation, layoutManager)
        recyclerAdapter.setHasStableIds(true)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = recyclerAdapter
        recyclerView.itemAnimator = null
        val folderDecoration = FolderItemDecoration(
            drawableNoChildren = ContextCompat.getDrawable(recyclerView.context, R.drawable.circle_gray)!!,
            drawableWithChildren = ContextCompat.getDrawable(recyclerView.context, R.drawable.rounded_rectangle_gray)!!
        )

        recyclerView.addItemDecoration(folderDecoration)

        val itemTouchHelper = ItemTouchHelper(DragAndDropTouchCallback(recyclerAdapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun onOperation(operation: Adapter.Operation) {
        when (operation) {
            is Adapter.Operation.Move -> {
                viewModel.move(operation.fromPosition, operation.toPosition)
            }
            is Adapter.Operation.Target -> {
                // no-op for now
//                viewModel.target(operation.sourcePosition, operation.targetPosition)
            }
        }.javaClass // force exhaustive
    }

    private fun onNewData(newData: List<Item>) {
        recyclerAdapter.setData(newData)
    }

    private fun bindViews() {
        recyclerView = findViewById(R.id.recycler_view)
    }

    companion object {
        private const val DISTANCE_FROM_CENTER_FOR_ADD = 0.2f
        private const val NUMBER_VIEW_SIZE_DP = 64
        private const val NUMBER_VIEW_MARGIN_DP = 4
    }

    class Adapter(
        private val onOperationRequested: (Operation) -> Unit,
        private val layoutManager: LinearLayoutManager
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), DragAndDropTouchCallback.Adapter {

        private var curComputedOperation: Operation? = null

        private var data: List<Item> = emptyList()

        fun setData(newData: List<Item>) {
            val diffUtilCallback = object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return data[oldItemPosition].id == newData[newItemPosition].id
                }

                override fun getOldListSize(): Int {
                    return data.size
                }

                override fun getNewListSize(): Int {
                    return newData.size
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return data[oldItemPosition] == newData[newItemPosition]
                }
            }

            val diffResult = DiffUtil.calculateDiff(diffUtilCallback, false)

            this.data = newData

            diffResult.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_PLACEHOLDER -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.placeholder, parent, false)
                    PlaceholderViewHolder(view)
                }
                VIEW_TYPE_FOLDER -> {
                    val view = LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.decorated_folder_list_item, parent, false) as DecoratedNumberFolderView
                    FolderViewHolder(view)
                }
                VIEW_TYPE_NUMBER -> {
                    val view = LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.colored_number_list_item, parent, false) as ColoredNumberView
                    NumberViewHolder(view)
                }
                else -> throw IllegalArgumentException("invalid view type: $viewType")
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }
        override fun getItemId(position: Int): Long {
            return data[position].id
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = data[position]) {
                is Item.PlaceholderListItem -> {
                    holder as PlaceholderViewHolder
                }
                is Item.FolderListItem -> {
                    (holder as FolderViewHolder).configure(item)
                }
                is Item.ColoredNumberListItem -> {
                    (holder as NumberViewHolder).configure(item)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (data[position]) {
                is Item.PlaceholderListItem -> VIEW_TYPE_PLACEHOLDER
                is Item.FolderListItem -> VIEW_TYPE_FOLDER
                is Item.ColoredNumberListItem -> VIEW_TYPE_NUMBER
            }
        }

        private class PlaceholderViewHolder(view: View) : RecyclerView.ViewHolder(view)

        class FolderViewHolder(view: DecoratedNumberFolderView) : RecyclerView.ViewHolder(view) {

            private var numChildren = 0

            fun getNumChildren() = numChildren.also { Log.d("findme", "returning numChildren: $numChildren") }

            fun configure(item: Item.FolderListItem) {
                itemView as DecoratedNumberFolderView

                itemView.setIsHighlighted(isHighlighted = item.isTargeted)

                itemView.setOnClickListener {
                    if (item.isOpen) {
                        ExpandedFolderRepository.collapseFolder(item.id)
                    } else {
                        ExpandedFolderRepository.expandFolder(item.id)
                    }
                }

                numChildren = item.numChildren
            }
        }

        private class NumberViewHolder(view: ColoredNumberView) : RecyclerView.ViewHolder(view), DragAndDropTouchCallback.DraggableViewHolder {

            override fun onDragStateChanged(dragging: Boolean) {
                // do nothin'
            }

            override fun canDrag(): Boolean {
                return true
            }

            fun configure(item: Item.ColoredNumberListItem) {
                itemView as ColoredNumberView
                itemView.configure(item.coloredNumber)
                itemView.setIsHighlighted(item.isTargeted)
            }
        }

        override fun isValidMove(fromPosition: Int, toPosition: Int): Boolean {
            return false
        }

        override fun onDragStarted(viewHolder: RecyclerView.ViewHolder?) {
            // on drag started?
        }

        override fun onDrop() {
            // on drop?
        }

        override fun onMoveTargeted(
            recyclerView: RecyclerView,
            source: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            when (val operation = curComputedOperation) {
                is Operation.Move -> {
                    onOperationRequested(operation)
                    return true
                }
                is Operation.Target -> {
                    onOperationRequested(operation)
                    return true
                }
            }

            return false
        }

        private val boundingBoxRect = Rect()
        override fun chooseDropTarget(
            selected: RecyclerView.ViewHolder,
            dropTargets: MutableList<RecyclerView.ViewHolder>,
            curX: Int,
            curY: Int
        ): RecyclerView.ViewHolder? {
            if (dropTargets.isEmpty()) {
                curComputedOperation = null
                return null
            }

            layoutManager.getTransformedBoundingBox(selected.itemView, false, boundingBoxRect)
            val selectedCenterY = boundingBoxRect.centerY()
            val target = dropTargets
                .sortedBy { potentialTarget ->
                    layoutManager.getTransformedBoundingBox(potentialTarget.itemView, false, boundingBoxRect)
                    val potentialTargetCenterY = boundingBoxRect.centerY()
                    Math.abs(selectedCenterY - potentialTargetCenterY)
                }
                .first()

            // could cache this somehow and save one computation, but doesn't seem necessary...
            layoutManager.getTransformedBoundingBox(target.itemView, false, boundingBoxRect)
            val targetCenterY = boundingBoxRect.centerY()

            val itemHeight = boundingBoxRect.height()
            val isCloseToCenter = Math.abs(selectedCenterY - targetCenterY) < (itemHeight * CENTER_THRESHOLD)

            val isMovingUp = selected.adapterPosition > target.adapterPosition
            val operation: Operation?
            if (isMovingUp && selectedCenterY < targetCenterY) {
                operation = Operation.Move(selected.adapterPosition, target.adapterPosition)
            } else if (!isMovingUp && selectedCenterY > targetCenterY) {
                operation = Operation.Move(selected.adapterPosition, target.adapterPosition + 1)
            } else {
                operation = null
            }
            curComputedOperation = operation

            return target
        }

        companion object {
            private const val VIEW_TYPE_PLACEHOLDER = 0
            private const val VIEW_TYPE_FOLDER = 1
            private const val VIEW_TYPE_NUMBER = 2

            private const val CENTER_THRESHOLD = 0f
        }

        sealed class Operation {
            data class Move(val fromPosition: Int, val toPosition: Int) : Operation()
            data class Target(val sourcePosition: Int, val targetPosition: Int) : Operation()
        }
    }
}
