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

    private var numbers: MutableList<Entry> = ArrayList()
    private val numbersSubject = BehaviorSubject.create<List<Entry>>()

    fun observeNumbers(): Observable<List<Entry>> = numbersSubject

    fun init() {
        numbers = generateData(30).toMutableList()
        publish()
    }

    private fun publish() {
        numbersSubject.onNext(numbers)
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