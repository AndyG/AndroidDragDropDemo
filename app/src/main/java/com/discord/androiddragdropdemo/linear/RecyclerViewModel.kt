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

class RecyclerViewModel : ViewModel() {

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

    fun move(fromPosition: Int, toPosition: Int) {
        val editingList = ArrayList(listItems)
        swapItems(editingList, fromPosition, toPosition)
        listItems.clear()
        listItems.addAll(editingList)
        publish()
    }

    private fun <T> swapItems(list: MutableList<T>, fromPos: Int, toPos: Int) {
        if (Math.abs(fromPos - toPos) == 1) {
            val temp = list[fromPos]
            list[fromPos] = list[toPos]
            list[toPos] = temp
        } else {
            val isUp = fromPos - toPos < 0
            val item = list[fromPos]

            list.removeAt(fromPos)

            val newInsertionIndex = if (isUp) toPos else toPos - 1
            list.add(newInsertionIndex, item)
        }
    }

    private fun onNewData(repositoryData: RepositoryData) {
        val (numbers, expandedFolderIds) = repositoryData

        val newListItems = ArrayList<Item>()

        numbers.forEach { entry ->
            when (entry) {
                is NumbersRepository.Entry.Folder -> {
                    val isOpen = entry.id in expandedFolderIds
                    val numChildren =
                        if (isOpen) entry.numbers.size
                        else 0

                    newListItems.add(Item.FolderListItem(isOpen = isOpen, numChildren = numChildren, id = entry.id, isTargeted = false))

                    if (isOpen) {
                        entry.numbers.forEach { child ->
                            val childListItem = Item.ColoredNumberListItem(
                                coloredNumber = child,
                                isTargeted = false,
                                folderId = entry.id
                            )
                            newListItems.add(childListItem)
                        }
                    }
                }
                is NumbersRepository.Entry.SingletonNumber -> {
                    val listItem = Item.ColoredNumberListItem(
                        coloredNumber = entry.number,
                        isTargeted = false,
                        folderId = null
                    )
                    newListItems.add(listItem)
                }
            }
        }

        if (newListItems != listItems) {
            listItems.clear()
            listItems.addAll(newListItems)
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