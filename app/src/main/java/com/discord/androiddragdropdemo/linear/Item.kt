package com.discord.androiddragdropdemo.linear

sealed class Item(open val id: Long) {

    object Placeholder : Item(-1L)

    data class Folder(val isOpen: Boolean, val numChildren: Int, val isGone: Boolean, override val id: Long) : Item(id)

    data class ColoredNumber(val number: Int, val color: Color, val isGone: Boolean, override val id: Long) : Item(id) {
        enum class Color {
            RED,
            BLUE,
            GREEN
        }
    }
}