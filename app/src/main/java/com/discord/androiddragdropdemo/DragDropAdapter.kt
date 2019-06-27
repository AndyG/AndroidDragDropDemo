package com.discord.androiddragdropdemo

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class DragDropAdapter(private val layoutManager: LinearLayoutManager)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(), DragAndDropTouchCallback.Adapter {

    private var curDragFromPos: Int? = null
    private var curDragToPos: Int? = null
    private var curTargetedItemPos: Int? = null

    private var curComputedDropOperation : DragAndDropOperation? = null

    private var draggingViewHolder: NumberItemViewHolder? = null

    private var items: MutableList<Any> = ArrayList()

    override fun onDragStarted(viewHolder: RecyclerView.ViewHolder?) {
        curComputedDropOperation = null
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

        if (target is NumberItemViewHolder
            && curComputedDropOperation is DragAndDropOperation.Move) {
            untargetCurrentlyTargetedItem()
            swapItems(fromPos, toPos)
            return true
        } else if (target is SumItemViewHolder
            && curComputedDropOperation is DragAndDropOperation.AddToSum) {
            untargetCurrentlyTargetedItem()
            targetItem(toPos)
        }
        return false
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
        val operation = curComputedDropOperation ?: return
        curComputedDropOperation = null

        if (operation !is DragAndDropOperation.AddToSum
            && operation !is DragAndDropOperation.CreateSum) {
            return
        }

        val curDragFromPos = curDragFromPos ?: return
        val curDragToPos = curDragToPos ?: return

        untargetCurrentlyTargetedItem()

        val curDragItem = items[curDragFromPos] as DragAndDropNumberItem
        val curTargetItem = items[curDragToPos]

        droppedViewHolder.onDroppedOverSum()
        this.draggingViewHolder = null

        items.removeAt(curDragFromPos)
        notifyItemRemoved(curDragFromPos)

        this.curDragFromPos = null
        this.curDragToPos = null

        val wasDraggingDown = curDragToPos > curDragFromPos
        val adjustedDropPos = if (wasDraggingDown) curDragToPos - 1 else curDragToPos

        val newItem: DragAndDropSumItem = when (curTargetItem) {
            is DragAndDropNumberItem -> {
                DragAndDropSumItem(curSum = curTargetItem.number + curDragItem.number, isTargeted = false, id = Random.nextLong())
            }
            is DragAndDropSumItem -> {
                curTargetItem.copy(curSum = curTargetItem.curSum + curDragItem.number)
            }
            else -> throw IllegalStateException("what")
        }

        items[adjustedDropPos] = newItem
        notifyItemChanged(curDragToPos - 1)
    }

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
            VIEW_TYPE_NUMBER -> NumberItemViewHolder(view, layoutManager)
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

    override fun chooseDropTarget(
        selected: RecyclerView.ViewHolder,
        dropTargets: MutableList<RecyclerView.ViewHolder>,
        curX: Int,
        curY: Int
    ): RecyclerView.ViewHolder? {
        selected as NumberItemViewHolder
        Log.d("calculating", "numDropTargets: ${dropTargets.size}")
        var isDraggingOverOtherNumber = false

        val sumTarget = dropTargets.firstOrNull { it is SumItemViewHolder } as? SumItemViewHolder
        if (sumTarget != null) {
            curComputedDropOperation = DragAndDropOperation.AddToSum
            val targetPos = sumTarget.adapterPosition
            val targetSum = (items[targetPos] as DragAndDropSumItem).curSum
            selected.configureWithSum(otherNumber = targetSum)
            return sumTarget
        } else if (dropTargets.size >= 1) {
            val closestTarget = dropTargets
                .sortedBy { Math.abs(selected.adapterPosition - it.adapterPosition) }
                .first() as NumberItemViewHolder

            val isMovingUp = selected.adapterPosition > closestTarget.adapterPosition
            val otherCenter = selected.getCenterY()
            val operation = closestTarget.getDragAndDropOperation(isMovingUp, otherCenter)
            if (operation is DragAndDropOperation.CreateSum) {
                selected.configureWithSum(otherNumber = closestTarget.data?.number)
                isDraggingOverOtherNumber = true
            }
            Log.d("calculating",
                    "\nisMovingUp: $isMovingUp" +
                    "\n${selected.adapterPosition}" +
                    "\n${closestTarget.adapterPosition}" +
                    "\nshouldSwap: $operation")

            curComputedDropOperation = operation
            if (operation != null) {
                if (!isDraggingOverOtherNumber) {
                    selected.configureWithSum(null)
                }
                return dropTargets.first()
            } else {
                selected.configureWithSum(null)
                return null
            }
        } else {
            Log.w("findme", "had a fall through case. dropTargets: $dropTargets")
        }

        selected.configureWithSum(null)
        return null
    }

    override fun onDropTargetSelected(viewHolder: RecyclerView.ViewHolder?,
                                      curY: Int) {
        Log.d("findme", "onDropTargetSelected: $curY")
        if (viewHolder is NumberItemViewHolder) {
            viewHolder.onHoveredOver(curY)
        }
    }

    sealed class DragAndDropOperation {
        object Move : DragAndDropOperation()
        object AddToSum : DragAndDropOperation()
        object CreateSum : DragAndDropOperation()
    }

    companion object {
        private const val VIEW_TYPE_NUMBER = 0
        private const val VIEW_TYPE_SUM = 1
    }
}