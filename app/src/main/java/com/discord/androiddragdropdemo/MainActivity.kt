package com.discord.androiddragdropdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.ItemTouchHelper


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView : RecyclerView

    private lateinit var adapter: DragDropAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        configureRecyclerView()
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun generateData(): List<Any> {
        return (1..50).map { index ->
            if (index % 10 == 0) {
                DragAndDropSumItem(index)
            } else {
                DragAndDropNumberItem(index)
            }
        }
    }

    private fun configureRecyclerView() {
        this.adapter = DragDropAdapter()
        recyclerView.adapter = this.adapter
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)
        this.adapter.setItems(generateData())

        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder, target: ViewHolder
                ): Boolean {
                    val fromPos = viewHolder.adapterPosition
                    val toPos = target.adapterPosition
                    adapter.swapItems(fromPos, toPos)
                    return true
                }

                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                }
            })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun bindViews() {
        this.recyclerView = findViewById(R.id.activity_main_recycler_view)
    }
}
