package com.discord.androiddragdropdemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DragDropAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.draggable_view,
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

    fun swapItems(fromPos: Int, toPos: Int) {
        val temp = items[fromPos]
        items[fromPos] = items[toPos]
        items[toPos] = temp
        notifyItemMoved(fromPos, toPos)
    }

    companion object {
        private const val VIEW_TYPE_NUMBER = 0
        private const val VIEW_TYPE_SUM = 1
    }
}