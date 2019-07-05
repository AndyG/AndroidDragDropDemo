package com.discord.androiddragdropdemo.linear

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.discord.androiddragdropdemo.R
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
        recyclerAdapter = Adapter()
        recyclerAdapter.setHasStableIds(true)
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = recyclerAdapter
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

    private class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
                        .inflate(R.layout.folder_list_item, parent, false) as NumberFolderView
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

        override fun getItemViewType(position: Int): Int = when (data[position]) {
            is Item.PlaceholderListItem -> VIEW_TYPE_PLACEHOLDER
            is Item.FolderListItem -> VIEW_TYPE_FOLDER
            is Item.ColoredNumberListItem -> VIEW_TYPE_NUMBER
        }

        private class PlaceholderViewHolder(view: View) : RecyclerView.ViewHolder(view)

        private class FolderViewHolder(view: NumberFolderView) : RecyclerView.ViewHolder(view) {

            fun configure(item: Item.FolderListItem) {
                itemView as NumberFolderView

                itemView.setIsHighlighted(isHighlighted = item.isTargeted)

                itemView.setOnClickListener {
                    if (item.isOpen) {
                        ExpandedFolderRepository.collapseFolder(item.id)
                    } else {
                        ExpandedFolderRepository.expandFolder(item.id)
                    }
                }
            }
        }

        private class NumberViewHolder(
            view: ColoredNumberView
        ) : RecyclerView.ViewHolder(view) {
            fun configure(item: Item.ColoredNumberListItem) {
                itemView as ColoredNumberView
                itemView.configure(item.coloredNumber)
                itemView.setIsHighlighted(item.isTargeted)
            }
        }

        companion object {
            private const val VIEW_TYPE_PLACEHOLDER = 0
            private const val VIEW_TYPE_FOLDER = 1
            private const val VIEW_TYPE_NUMBER = 2
        }
    }
}
