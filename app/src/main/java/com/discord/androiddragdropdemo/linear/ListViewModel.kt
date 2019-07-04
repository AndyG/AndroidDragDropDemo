package com.discord.androiddragdropdemo.linear

import androidx.lifecycle.ViewModel
import com.discord.androiddragdropdemo.repository.NumbersRepository
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

class ListViewModel : ViewModel() {

    private val listItems = ArrayList<Item>()
    private val listItemsSubject = BehaviorSubject.create<List<Item>>()

    private val disposable: Disposable

    init {
        disposable = NumbersRepository
            .observeNumbers()
            .subscribe { numbers -> onNewData(numbers) }
    }

    fun observeListItems(): Observable<List<Item>> = listItemsSubject

    private fun onNewData(numbers: List<NumbersRepository.Entry>) {
        listItems.clear()

        numbers.forEach { entry ->
            when (entry) {
                is NumbersRepository.Entry.Folder -> TODO()
                is NumbersRepository.Entry.SingletonNumber ->
                    listItems.add(Item.ColoredNumberListItem(
                        coloredNumber = entry.number,
                        isTargeted = false
                    ))
            }
        }

        publish()
    }

    private fun publish() {
        listItemsSubject.onNext(listItems)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}