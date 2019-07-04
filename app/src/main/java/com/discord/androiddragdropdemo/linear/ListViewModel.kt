package com.discord.androiddragdropdemo.linear

import androidx.lifecycle.ViewModel
import com.discord.androiddragdropdemo.repository.ExpandedFolderRepository
import com.discord.androiddragdropdemo.repository.NumbersRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class ListViewModel : ViewModel() {

    private val listItems = ArrayList<Item>()
    private val listItemsSubject = BehaviorSubject.create<List<Item>>()

    private val disposable: Disposable

    private data class RepositoryData(
        val numbers: List<NumbersRepository.Entry>,
        val expandedFolderIds: Set<Long>
    )

    init {
        val repositoryObservable = Observables.combineLatest(
            NumbersRepository
                .observeNumbers(),
            ExpandedFolderRepository
                .observeExpandedFolderIds())
        { numbers, expandedFolderIds -> RepositoryData(numbers, expandedFolderIds) }

        disposable = repositoryObservable
            .debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onNewData)
    }

    fun observeListItems(): Observable<List<Item>> = listItemsSubject

    private fun onNewData(repositoryData: RepositoryData) {
        listItems.clear()

        val (numbers, expandedFolderIds) = repositoryData

        numbers.forEach { entry ->
            when (entry) {
                is NumbersRepository.Entry.Folder -> {
                    val isOpen = entry.id in expandedFolderIds
                    val numChildren =
                        if (isOpen) entry.numbers.size
                        else 0

                    listItems.add(Item.FolderListItem(isOpen = isOpen, numChildren = numChildren, id = entry.id))

                    if (isOpen) {
                        entry.numbers.forEach { child ->
                            val childListItem = Item.ColoredNumberListItem(
                                coloredNumber = child,
                                isTargeted = false
                            )
                            listItems.add(childListItem)
                        }
                    }
                }
                is NumbersRepository.Entry.SingletonNumber -> {
                    val listItem = Item.ColoredNumberListItem(
                        coloredNumber = entry.number,
                        isTargeted = false
                    )
                    listItems.add(listItem)
                }
            }
        }

        publish()
    }

    private fun publish() {
        listItemsSubject.onNext(ArrayList(listItems))
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}