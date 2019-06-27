package com.discord.androiddragdropdemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DragDropAdapter : RecyclerView.Adapter<AppViewHolder>() {

    private var items: MutableList<DragAndDropItem> = ArrayList()

    fun setItems(items: List<DragAndDropItem>) {
        this.items = items.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.draggable_view,
            parent,
            false)

        return AppViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.configure(items[position])
    }

    fun swapItems(fromPos: Int, toPos: Int) {
        val temp = items[fromPos]
        items[fromPos] = items[toPos]
        items[toPos] = temp
        notifyItemMoved(fromPos, toPos)
    }
}