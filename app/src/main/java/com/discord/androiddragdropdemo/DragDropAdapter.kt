package com.discord.androiddragdropdemo

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DragDropAdapter
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(), DragAndDropTouchCallback.Adapter {

    private var curDragFromPos: Int? = null
    private var curDragToPos: Int? = null
    private var curTargetedItemPos: Int? = null

    private var draggingViewHolder: NumberItemViewHolder? = null

    override fun onDragStarted(viewHolder: RecyclerView.ViewHolder?) {
        Log.d("findme", "onDragStarted")
        draggingViewHolder = viewHolder as NumberItemViewHolder
    }

    override fun onMoveTargeted(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPos = source.adapterPosition
        val toPos = target.adapterPosition

        curDragFromPos = fromPos
        curDragToPos = toPos

        if (target is NumberItemViewHolder) {
            untargetCurrentlyTargetedItem()
            swapItems(fromPos, toPos)
        } else if (target is SumItemViewHolder) {
            untargetCurrentlyTargetedItem()
            targetItem(toPos)
        }
        return true
    }

    private fun targetItem(pos: Int) {
        val item = items[pos] as DragAndDropSumItem
        items[pos] = item.copy(isTargeted = true)
        notifyItemChanged(pos)
        curTargetedItemPos = pos
    }

    private fun untargetCurrentlyTargetedItem() {
        val targetPos = this.curTargetedItemPos ?: return
        val item = items[targetPos] as DragAndDropSumItem
        items[targetPos] = item.copy(isTargeted = false)
        notifyItemChanged(targetPos)
        curTargetedItemPos = null
    }

    override fun isValidMove(fromPosition: Int, toPosition: Int): Boolean {
        return items[toPosition] is DragAndDropNumberItem
    }

    override fun onDrop() {
        val droppedViewHolder = draggingViewHolder ?: return
        this.draggingViewHolder = null
        val curDragFromPos = curDragFromPos ?: return
        val curDragToPos = curDragToPos ?: return

        untargetCurrentlyTargetedItem()

        Log.d("findme", "onDrop")

        val curDragItem = items[curDragFromPos] as DragAndDropNumberItem
        val curTargetItem = items[curDragToPos] as? DragAndDropSumItem ?: return //no-op if not a sum

        droppedViewHolder.onDroppedOverSum()
        this.draggingViewHolder = null

        items.removeAt(curDragFromPos)
        notifyItemRemoved(curDragFromPos)

        this.curDragFromPos = null
        this.curDragToPos = null

        val wasDraggingDown = curDragToPos > curDragFromPos
        val adjustedDropPos = if (wasDraggingDown) curDragToPos - 1 else curDragToPos

        items[adjustedDropPos] = curTargetItem.copy(curSum = curTargetItem.curSum + curDragItem.number)
        notifyItemChanged(curDragToPos - 1)
    }

    private var items: MutableList<Any> = ArrayList()

    fun setItems(items: List<Any>) {
        this.items = items.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is DragAndDropNumberItem -> VIEW_TYPE_NUMBER
            is DragAndDropSumItem -> VIEW_TYPE_SUM
            else -> throw IllegalStateException("invalid item: $item")
        }
    }

    override fun getItemId(position: Int): Long {
        return when (val item = items[position]) {
            is DragAndDropNumberItem -> item.id
            is DragAndDropSumItem -> item.id
            else -> throw IllegalStateException("invalid item: $item")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewId = when (viewType) {
            VIEW_TYPE_NUMBER -> R.layout.number_list_item
            VIEW_TYPE_SUM -> R.layout.sum_list_item
            else -> throw IllegalStateException("invalid view type: $viewType")
        }

        val view = LayoutInflater.from(parent.context).inflate(
            viewId,
            parent,
            false)

        return when (viewType) {
            VIEW_TYPE_NUMBER -> NumberItemViewHolder(view)
            VIEW_TYPE_SUM -> SumItemViewHolder(view)
            else -> throw IllegalStateException("invalid view type: $viewType")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NumberItemViewHolder -> holder.configure(items[position] as DragAndDropNumberItem)
            is SumItemViewHolder -> holder.configure(items[position] as DragAndDropSumItem)
            else -> throw IllegalStateException("Invalid view holder: ${holder.javaClass}")
        }
    }

    private fun swapItems(fromPos: Int, toPos: Int) {
        if (Math.abs(fromPos - toPos) == 1) {
            val temp = items[fromPos]
            items[fromPos] = items[toPos]
            items[toPos] = temp
            notifyItemMoved(fromPos, toPos)
        } else {
            val isUp = fromPos - toPos < 0
            val item = items[fromPos]

            items.removeAt(fromPos)
            notifyItemRemoved(fromPos)

            val newInsertionIndex = if (isUp) toPos else toPos - 1
            items.add(newInsertionIndex, item)
            notifyItemInserted(newInsertionIndex)
        }
    }

    override fun getOverridenDropTarget(
        selected: RecyclerView.ViewHolder,
        dropTargets: MutableList<RecyclerView.ViewHolder>,
        curX: Int,
        curY: Int
    ): RecyclerView.ViewHolder? {
        Log.d("findme", "dropTargets size: ${dropTargets.size}. curY: $curY")
        return dropTargets.firstOrNull { it is SumItemViewHolder }
    }

    companion object {
        private const val VIEW_TYPE_NUMBER = 0
        private const val VIEW_TYPE_SUM = 1
    }
}