package com.discord.androiddragdropdemo.linear

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.discord.androiddragdropdemo.R
import com.discord.androiddragdropdemo.utils.dpToPx
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class NumberFolderActivity : AppCompatActivity() {

    private lateinit var numberFolderView: NumberFolderView

    private var itemSize: Float = 0f

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_folder)

        val numberSize = dpToPx(NUMBER_VIEW_SIZE_DP, resources).toInt()
        val margin = dpToPx(NUMBER_VIEW_MARGIN_DP, resources).toInt()
        itemSize = numberSize + (margin * 2).toFloat()

        bindViews()
        beginChangingHeightOfFolder()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private fun bindViews() {
        this.numberFolderView = findViewById(R.id.number_folder_view)
    }

    private fun beginChangingHeightOfFolder() {
        disposable = Observable
            .interval(1000L, 1000L, TimeUnit.MILLISECONDS)
            .map { it.toInt() % 5 }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { numberFolderView.setNumChildren(it, itemSize, 0f) }
    }

    companion object {
        private const val NUMBER_VIEW_SIZE_DP = 64
        private const val NUMBER_VIEW_MARGIN_DP = 4
    }
}
