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

    private var draggedItem: Item? = null

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

    fun onDragStarted(item: Item) {
        draggedItem = item

        val editingList = ArrayList(listItems)
        val itemIndex = editingList.indexOf(item)

        editingList.apply {
            set(itemIndex, Item.PlaceholderListItem)
        }

        listItems.clear()
        listItems.addAll(editingList)
        publish()
    }

    fun targetItem(item: Item, targetType: TargetType, inFolder: Boolean) {
        when (targetType) {
            TargetType.BELOW -> {
                val editingList = ArrayList(listItems)

                // untarget old target.
                val oldTargetIndex = editingList.indexOfFirst { it is Item.ColoredNumberListItem && it.isTargeted }
                if (oldTargetIndex > -1) {
                    val oldTarget = editingList[oldTargetIndex]
                    editingList[oldTargetIndex] = when (oldTarget) {
                        is Item.ColoredNumberListItem -> oldTarget.copy(isTargeted = false)
                        else -> TODO("unsupported targeting of other item types")
                    }
                }

                val targetIndex = editingList.indexOf(item)
                val existingPlaceholderIndex = editingList.indexOf(Item.PlaceholderListItem)
                editingList.removeAt(existingPlaceholderIndex)
                // adjust for removal.
                val adjustedTargetIndex =
                    if (existingPlaceholderIndex < targetIndex) targetIndex - 1
                    else targetIndex

                // it's a BELOW target, so add 1.
                editingList.add(adjustedTargetIndex + 1, Item.PlaceholderListItem)
                listItems.clear()
                listItems.addAll(editingList)
                publish()
            }
            TargetType.ABOVE -> {
                val editingList = ArrayList(listItems)

                // untarget old target.
                val oldTargetIndex = editingList.indexOfFirst { it is Item.ColoredNumberListItem && it.isTargeted }
                if (oldTargetIndex > -1) {
                    val oldTarget = editingList[oldTargetIndex]
                    editingList[oldTargetIndex] = when (oldTarget) {
                        is Item.ColoredNumberListItem -> oldTarget.copy(isTargeted = false)
                        else -> TODO("unsupported targeting of other item types")
                    }
                }

                val targetIndex = editingList.indexOf(item)
                val existingPlaceholderIndex = editingList.indexOf(Item.PlaceholderListItem)
                editingList.removeAt(existingPlaceholderIndex)
                // adjust for removal.
                val adjustedTargetIndex =
                    if (existingPlaceholderIndex < targetIndex) targetIndex - 1
                    else targetIndex

                editingList.add(adjustedTargetIndex, Item.PlaceholderListItem)
                listItems.clear()
                listItems.addAll(editingList)
                publish()
            }
            TargetType.INSIDE -> {
                val editingList = ArrayList(listItems)

                val oldTargetIndex = editingList.indexOfFirst { it is Item.ColoredNumberListItem && it.isTargeted }
                val targetIndex = editingList.indexOf(item)

                if (oldTargetIndex == targetIndex) {
                    return
                }

                // untarget old target.
                if (oldTargetIndex > -1) {
                    val oldTarget = editingList[oldTargetIndex]
                    editingList[oldTargetIndex] = when (oldTarget) {
                        is Item.ColoredNumberListItem -> oldTarget.copy(isTargeted = false)
                        else -> TODO("unsupported targeting of other item types")
                    }
                }

                if (item is Item.ColoredNumberListItem // can only target numbers
                    && !item.isTargeted // if this is already targeted, forget about it
                    && item.folderId == null // no targeting numbers in folders
                ) {
                    editingList[targetIndex] = item.copy(isTargeted = true)
                }

                listItems.clear()
                listItems.addAll(editingList)
                publish()
            }
        }
    }

    enum class TargetType {
        INSIDE,
        ABOVE,
        BELOW
    }

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
                                isTargeted = false,
                                folderId = entry.id
                            )
                            listItems.add(childListItem)
                        }
                    }
                }
                is NumbersRepository.Entry.SingletonNumber -> {
                    val listItem = Item.ColoredNumberListItem(
                        coloredNumber = entry.number,
                        isTargeted = false,
                        folderId = null
                    )
                    listItems.add(listItem)
                }
            }
        }

        publish()
    }

    fun onDragEnded() {
        val editingList = ArrayList(listItems)
        val placeholderIndex = editingList.indexOf(Item.PlaceholderListItem)
        editingList[placeholderIndex] = draggedItem
        draggedItem = null

        listItems.clear()
        listItems.addAll(editingList)
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