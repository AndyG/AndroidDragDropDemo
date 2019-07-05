package com.discord.androiddragdropdemo.linear

import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
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
    private var targetedIndex: Int? = null

    private var placeholderFolderId: Long? = null

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
        val itemIndex = editingList.indexOfFirst { it.id == item.id }

        editingList.apply {
            set(itemIndex, Item.PlaceholderListItem((item as Item.ColoredNumberListItem).folderId))
        }

        listItems.clear()
        listItems.addAll(editingList)
        publish()
    }

    fun targetItem(item: Item, targetType: TargetType) {
        val existingPlaceholderIndex = listItems.indexOfFirst { it.id == Item.PLACEHOLDER_ID }
        val oldPlaceholderFolderId = this.placeholderFolderId

        when (targetType) {
            TargetType.BELOW -> {
                val editingList = ArrayList(listItems)

                // untarget old target.
                val oldTargetIndex = targetedIndex ?: -1
                if (oldTargetIndex > -1) {
                    val oldTarget = editingList[oldTargetIndex]
                    editingList[oldTargetIndex] = when (oldTarget) {
                        is Item.ColoredNumberListItem -> oldTarget.copy(isTargeted = false)
                        is Item.FolderListItem -> oldTarget.copy(isTargeted = false)
                        else -> TODO("unsupported targeting of other item types")
                    }

                    targetedIndex = null
                }

                val targetIndex = editingList.indexOfFirst { it.id == item.id }
                val newPlaceholderFolderId = when(val targetItem = editingList[targetIndex]) {
                    is Item.ColoredNumberListItem -> targetItem.folderId // todo: allow not inserting in folder
                    is Item.FolderListItem -> {
                        if (targetItem.isOpen) {
                            targetItem.id
                        } else {
                            null
                        }
                    }
                    else -> throw IllegalStateException("wut")
                }

                editingList.removeAt(existingPlaceholderIndex)
                // adjust for removal.
                val adjustedTargetIndex =
                    if (existingPlaceholderIndex < targetIndex) targetIndex - 1
                    else targetIndex

                // it's a BELOW target, so add 1.
                editingList.add(adjustedTargetIndex + 1, Item.PlaceholderListItem(newPlaceholderFolderId))

                if (newPlaceholderFolderId != oldPlaceholderFolderId) {
                    if (oldPlaceholderFolderId != null) {
                        val oldFolderItemIndex = editingList
                            .indexOfFirst { it.id == oldPlaceholderFolderId }
                        val oldFolderItem = editingList[oldFolderItemIndex] as Item.FolderListItem
                        editingList[oldFolderItemIndex] = oldFolderItem.copy(numChildren = oldFolderItem.numChildren - 1)
                    }

                    if (newPlaceholderFolderId != null) {
                        val newFolderItemIndex = editingList
                            .indexOfFirst { it.id == newPlaceholderFolderId }
                        val newFolderItem = editingList[newFolderItemIndex] as Item.FolderListItem
                        editingList[newFolderItemIndex] = newFolderItem.copy(numChildren = newFolderItem.numChildren + 1)
                    }
                }

                this.placeholderFolderId = newPlaceholderFolderId
                listItems.clear()
                listItems.addAll(editingList)
                publish()
            }
            TargetType.ABOVE -> {
                val editingList = ArrayList(listItems)

                // untarget old target.
                val oldTargetIndex = targetedIndex ?: -1
                if (oldTargetIndex > -1) {
                    val oldTarget = editingList[oldTargetIndex]
                    editingList[oldTargetIndex] = when (oldTarget) {
                        is Item.ColoredNumberListItem -> oldTarget.copy(isTargeted = false)
                        is Item.FolderListItem -> oldTarget.copy(isTargeted = false)
                        else -> TODO("unsupported targeting of other item types")
                    }

                    targetedIndex = null
                }

                val targetIndex = editingList.indexOfFirst { it.id == item.id }
                val targetItem = editingList[targetIndex]
                val newPlaceholderFolderId = when (targetItem) {
                    is Item.PlaceholderListItem -> throw IllegalStateException("wut")
                    is Item.FolderListItem -> null
                    is Item.ColoredNumberListItem -> targetItem.folderId
                }

                editingList.removeAt(existingPlaceholderIndex)
                // adjust for removal.
                val adjustedTargetIndex =
                    if (existingPlaceholderIndex < targetIndex) targetIndex - 1
                    else targetIndex

                editingList.add(adjustedTargetIndex, Item.PlaceholderListItem(newPlaceholderFolderId))

                if (newPlaceholderFolderId != oldPlaceholderFolderId) {
                    if (oldPlaceholderFolderId != null) {
                        val oldFolderItemIndex = editingList
                            .indexOfFirst { it.id == oldPlaceholderFolderId }
                        val oldFolderItem = editingList[oldFolderItemIndex] as Item.FolderListItem
                        editingList[oldFolderItemIndex] = oldFolderItem.copy(numChildren = oldFolderItem.numChildren - 1)
                    }

                    if (newPlaceholderFolderId != null) {
                        val newFolderItemIndex = editingList
                            .indexOfFirst { it.id == newPlaceholderFolderId }
                        val newFolderItem = editingList[newFolderItemIndex] as Item.FolderListItem
                        editingList[newFolderItemIndex] = newFolderItem.copy(numChildren = newFolderItem.numChildren + 1)
                    }
                }

                this.placeholderFolderId = newPlaceholderFolderId
                listItems.clear()
                listItems.addAll(editingList)
                publish()
            }
            TargetType.INSIDE -> {
                val oldTargetIndex = targetedIndex ?: -1
                val targetIndex = listItems.indexOfFirst { it.id == item.id }

                if (oldTargetIndex == targetIndex) {
                    return
                }

                targetedIndex = targetIndex

                val editingList = ArrayList(listItems)

                // untarget old target.
                if (oldTargetIndex > -1) {
                    val oldTarget = editingList[oldTargetIndex]
                    editingList[oldTargetIndex] = when (oldTarget) {
                        is Item.ColoredNumberListItem -> oldTarget.copy(isTargeted = false)
                        is Item.FolderListItem -> oldTarget.copy(isTargeted = false)
                        else -> TODO("unsupported targeting of other item types")
                    }
                }

                if (item is Item.ColoredNumberListItem // can only target numbers
                    && !item.isTargeted // if this is already targeted, forget about it
                    && item.folderId == null // no targeting numbers in folders
                ) {
                    editingList[targetIndex] = item.copy(isTargeted = true)
                } else if (item is Item.FolderListItem
                    && !item.isTargeted
                    && !item.isOpen) {
                    editingList[targetIndex] = item.copy(isTargeted = true)
                }

                listItems.clear()
                listItems.addAll(editingList)
                publish()
            }
        }
    }

    fun ensureNoTarget() {
        val oldTargetIndex = targetedIndex ?: -1

        // untarget old target.
        if (oldTargetIndex > -1) {
            val editingList = ArrayList(listItems)
            val oldTarget = listItems[oldTargetIndex]
            editingList[oldTargetIndex] = when (oldTarget) {
                is Item.ColoredNumberListItem -> oldTarget.copy(isTargeted = false)
                is Item.FolderListItem -> oldTarget.copy(isTargeted = false)
                else -> TODO("unsupported targeting of other item types")
            }

            targetedIndex = null

            listItems.clear()
            listItems.addAll(editingList)
            publish()
        }
    }

    enum class TargetType {
        INSIDE,
        ABOVE,
        BELOW
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

    fun onDragEnded() {
        val draggedItemCapture = draggedItem as Item.ColoredNumberListItem
        val editingList = ArrayList(listItems)

        val placeholderIndex = editingList.indexOfFirst { it.id == Item.PLACEHOLDER_ID }

        if (placeholderIndex < 0) {
            throw IllegalStateException("no placeholder when drag ended")
        }

        val targetItemIndex = targetedIndex ?: -1

        if (targetItemIndex > -1) {
            when (val targetedItem = listItems[targetItemIndex]) {
                is Item.ColoredNumberListItem -> {
                    val sum = draggedItemCapture.coloredNumber.number + targetedItem.coloredNumber.number
                    val sumColoredNumber = targetedItem.coloredNumber.copy(number = sum)
                    val sumListItem = targetedItem.copy(coloredNumber = sumColoredNumber, isTargeted = false)
                    editingList[targetItemIndex] = sumListItem
                    editingList.removeAt(placeholderIndex)

                    listItems.clear()
                    listItems.addAll(editingList)

                    draggedItem = null
                    placeholderFolderId = null
                    targetedIndex = null

                    publish()
                    NumbersRepository.joinNumber(draggedItemCapture.id, targetedItem.id)
                }
                is Item.FolderListItem -> {
                    editingList[targetItemIndex] = targetedItem.copy(isTargeted = false)
                    editingList.removeAll { it.id == draggedItemCapture.id }
                    editingList.removeAt(placeholderIndex)

                    listItems.clear()
                    listItems.addAll(editingList)

                    draggedItem = null
                    placeholderFolderId = null
                    targetedIndex = null

                    publish()
                    NumbersRepository.addNumberToFolder(draggedItemCapture.id, targetedItem.id, belowId = null)
                }
                else -> throw IllegalStateException("invalid targeted item")
            }
        } else {
            editingList[placeholderIndex] = draggedItem

            val belowId: Long?

            if (placeholderIndex == 0) {
                belowId = null
            } else {
                val aboveItem = editingList[placeholderIndex - 1]
                belowId = when (aboveItem) {
                    is Item.FolderListItem -> aboveItem.id
                    is Item.ColoredNumberListItem -> aboveItem.folderId ?: aboveItem.id
                    else -> throw IllegalArgumentException("unexpected above item")
                }
            }

            listItems.clear()
            listItems.addAll(editingList)

            publish()

            draggedItem = null
            placeholderFolderId = null
            targetedIndex = null
            NumbersRepository.moveNumber(draggedItemCapture.id, belowId, folderId = null)
        }
    }

    private fun publish() {
        listItemsSubject.onNext(ArrayList(listItems))
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}