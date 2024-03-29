package com.discord.androiddragdropdemo.recycler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.discord.androiddragdropdemo.data.DragAndDropNumberItem
import com.discord.androiddragdropdemo.data.DragAndDropSumItem
import com.discord.androiddragdropdemo.R


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
                DragAndDropSumItem(index, isTargeted = false, id = index.toLong())
            } else {
                DragAndDropNumberItem(index, id = index.toLong())
            }
        }
    }

    private fun configureRecyclerView() {
        val lm = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)
        this.adapter = DragDropAdapter(lm)
        adapter.setHasStableIds(true)
        recyclerView.adapter = this.adapter
        recyclerView.layoutManager = lm
        this.adapter.setItems(generateData())

        val itemTouchHelper = ItemTouchHelper(DragAndDropTouchCallback(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun bindViews() {
        this.recyclerView = findViewById(R.id.activity_main_recycler_view)
    }
}
