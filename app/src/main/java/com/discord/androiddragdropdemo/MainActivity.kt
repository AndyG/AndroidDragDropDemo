package com.discord.androiddragdropdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
                DragAndDropSumItem(index, isTargeted = false, id = index.toLong())
            } else {
                DragAndDropNumberItem(index, id = index.toLong())
            }
        }
    }

    private fun configureRecyclerView() {
        this.adapter = DragDropAdapter()
        adapter.setHasStableIds(true)
        recyclerView.adapter = this.adapter
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)
        this.adapter.setItems(generateData())

        val itemTouchHelper = ItemTouchHelper(
            DragAndDropTouchCallback(adapter,
            itemHeight = dpToPixels(64)))

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun bindViews() {
        this.recyclerView = findViewById(R.id.activity_main_recycler_view)
    }

    private fun dpToPixels(dp: Int) =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        )
}
