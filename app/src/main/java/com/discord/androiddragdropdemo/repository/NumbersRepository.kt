package com.discord.androiddragdropdemo.repository

import com.discord.androiddragdropdemo.domain.ColoredNumber
import com.discord.androiddragdropdemo.utils.generateId
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

object NumbersRepository {

    sealed class Entry {
        data class Folder(val id: Long, val numbers: List<ColoredNumber>) : Entry()
        data class SingletonNumber(val number: ColoredNumber) : Entry()
    }

    private var entries: MutableList<Entry> = ArrayList()
    private val entriesSubject = BehaviorSubject.create<List<Entry>>()

    fun observeNumbers(): Observable<List<Entry>> = entriesSubject

    fun init() {
        entries = generateData(30).toMutableList()
        publish()
    }

    fun moveNumber(id: Long, belowId: Long?, folderId: Long?) {
        assert(folderId == null)
        // Find the entry representing this number.
        val entryIndex = entries.indexOfFirst { it is Entry.SingletonNumber && it.number.id == id }
        val entry = entries[entryIndex]

        // Find the index we want to move it below.
        val belowIndex = belowId?.let {
            entries.indexOfFirst { entry ->
                entry is Entry.SingletonNumber && entry.number.id == belowId
                        || entry is Entry.Folder && entry.id == belowId
            }
        } ?: -1

        // This is a bit cheeky, it also offsets the -1 if belowId was null.
        val insertionIndex = belowIndex + 1
        entries.add(insertionIndex, entry)

        // Remove the old entry.
        val adjustedRemovalIndex =
                if (insertionIndex <= entryIndex) entryIndex + 1
                else entryIndex

        entries.removeAt(adjustedRemovalIndex)
        publish()
    }

    fun joinNumber(sourceId: Long, targetId: Long) {
        // Find the entry representing the source.
        val sourceIndex = entries.indexOfFirst { it is Entry.SingletonNumber && it.number.id == sourceId }
        val source = (entries[sourceIndex] as Entry.SingletonNumber).number

        // Find the entry representing the target.
        val targetIndex = entries.indexOfFirst { it is Entry.SingletonNumber && it.number.id == targetId }
        val target = (entries[targetIndex] as Entry.SingletonNumber).number

        val sum = source.number + target.number
        val newItem = target.copy(number = sum)

        entries[targetIndex] = NumbersRepository.Entry.SingletonNumber(newItem)
        entries.removeAt(sourceIndex)
        publish()
    }

    private fun publish() {
        entriesSubject.onNext(entries)
    }

    private fun generateData(count: Int): List<Entry> {
        return (1..count).map { index ->
            if (index % 5 == 0) {
                val coloredNumbers = (0..2).map { it + 100 + index }.map(::generateColoredNumber)
                NumbersRepository.Entry.Folder(
                    id = generateId(),
                    numbers = coloredNumbers
                )
            } else {
                val coloredNumber = generateColoredNumber(index)
                NumbersRepository.Entry.SingletonNumber(coloredNumber)
            }
        }
    }

    private fun generateColoredNumber(number: Int): ColoredNumber {
        return ColoredNumber(
            id = generateId(),
            color = when (number % 3) {
                0 -> ColoredNumber.Color.RED
                1 -> ColoredNumber.Color.GREEN
                2 -> ColoredNumber.Color.BLUE
                else -> throw IllegalStateException("unexpected color")
            },
            number = number
        )
    }
}