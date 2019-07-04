package com.discord.androiddragdropdemo.linear

sealed class Item(open val id: Long) {

    object Placeholder : Item(-1L)

    data class Folder(val isOpen: Boolean, val numChildren: Int, override val id: Long) : Item(id)

    data class ColoredNumber(val number: Int, val color: Color, override val id: Long, val isTargeted: Boolean = false) : Item(id) {
        enum class Color {
            RED,
            BLUE,
            GREEN
        }
    }
}