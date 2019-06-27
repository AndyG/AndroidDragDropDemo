package com.discord.androiddragdropdemo

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


/**
 * Enables drag/drop functionality when attached to a RecyclerView.Adapter.
 */
class DragAndDropTouchCallback @JvmOverloads constructor(
    private val adapter: Adapter,
    private val dragScrollSpeed: Int = DEFAULT_DRAG_SCROLL_SPEED,
    private val itemHeight: Float
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val canDrag = (viewHolder as DraggableViewHolder).canDrag()

        val dragFlags = if (canDrag) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return adapter.onMoveTargeted(recyclerView, source, target)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Let the view holder know that this item is being moved or dragged.
            (viewHolder as? DraggableViewHolder)?.onDragStateChanged(true)
            adapter.onDragStarted(viewHolder)
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            // Tell the view holder to restore the idle state.
            (viewHolder as? DraggableViewHolder)?.onDragStateChanged(false)

            adapter.onDrop()
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {}

    override fun interpolateOutOfBoundsScroll(
        recyclerView: RecyclerView,
        viewSize: Int,
        viewSizeOutOfBounds: Int,
        totalSize: Int,
        msSinceStartScroll: Long
    ): Int {
        val direction = Math.signum(viewSizeOutOfBounds.toFloat()).toInt()
        return dragScrollSpeed * direction
    }

    override fun chooseDropTarget(
        selected: RecyclerView.ViewHolder,
        dropTargets: MutableList<RecyclerView.ViewHolder>,
        curX: Int,
        curY: Int
    ): RecyclerView.ViewHolder? {
        val adapterOverride = adapter.getOverridenDropTarget(selected, dropTargets, curX, curY)
        return when {
            adapterOverride != null -> adapterOverride
            else -> super.chooseDropTarget(selected, dropTargets, curX, curY)
        }
    }

//    override fun onChildDraw(
//        c: Canvas,
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder,
//        dX: Float,
//        dY: Float,
//        actionState: Int,
//        isCurrentlyActive: Boolean
//    ) {
//        if (isCurrentlyActive || adapter.shouldDrawRecoveringView()) {
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//        } else {
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//        }
//    }

    /**
     * Lets the adapter know when an item was moved. onDrop does not let the adapter know the position
     * the item was dropped at -- it is up to the adapter to keep track of that, which lets the adapter
     * decide what positions are valid drop points.
     */
    interface Adapter {
        fun isValidMove(fromPosition: Int, toPosition: Int): Boolean
        fun onDragStarted(viewHolder: RecyclerView.ViewHolder?)
        fun onDrop()
        fun onMoveTargeted(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
        fun getOverridenDropTarget(
            selected: RecyclerView.ViewHolder,
            dropTargets: MutableList<RecyclerView.ViewHolder>,
            curX: Int,
            curY: Int
        ): RecyclerView.ViewHolder?
    }

    interface DraggableViewHolder {
        fun onDragStateChanged(dragging: Boolean)
        fun canDrag(): Boolean
    }

    companion object {

        private const val DEFAULT_DRAG_SCROLL_SPEED = 15
    }
}

