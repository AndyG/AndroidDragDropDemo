package com.discord.androiddragdropdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
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

    private fun configureRecyclerView() {
        this.adapter = DragDropAdapter()
        recyclerView.adapter = this.adapter
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)
        this.adapter.setItems((1..50).map { DragAndDropItem(it) })

        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder, target: ViewHolder
                ): Boolean {
                    val fromPos = viewHolder.adapterPosition
                    val toPos = target.adapterPosition
                    adapter.swapItems(fromPos, toPos)
                    Log.d("findme", "fromPos: $fromPos -- toPos: $toPos")
                    // move item in `fromPos` to `toPos` in adapter.
                    return true// true if moved, false otherwise
                }

                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    // remove from adapter
                }
            })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun bindViews() {
        this.recyclerView = findViewById(R.id.activity_main_recycler_view)
    }
}
