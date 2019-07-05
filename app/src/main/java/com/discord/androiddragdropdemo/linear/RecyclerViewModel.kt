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

    private var curTargetPosition: Int? = null

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
        untargetCurrentTarget(editingList)
        val item = editingList.removeAt(fromPosition)

        val adjustedToPosition = when {
            fromPosition < toPosition -> toPosition - 1
            else -> toPosition
        }

        editingList.add(adjustedToPosition, item)
        listItems.clear()
        listItems.addAll(editingList)
        publish()
    }

    fun target(fromPosition: Int, targetPosition: Int) {
        val editingList = ArrayList(listItems)
        untargetCurrentTarget(editingList)
        val targetedItem = editingList[targetPosition] as Item.ColoredNumberListItem
        editingList[targetPosition] = targetedItem.copy(isTargeted = true)
        curTargetPosition = targetPosition
        listItems.clear()
        listItems.addAll(editingList)
        publish()
    }

    private fun untargetCurrentTarget(editingList: MutableList<Item>) {
        val curTargetPosition = this.curTargetPosition
        if (curTargetPosition != null) {
            val targetedItem = editingList[curTargetPosition] as Item.ColoredNumberListItem
            editingList[curTargetPosition] = targetedItem.copy(isTargeted = false)
            this.curTargetPosition = null
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