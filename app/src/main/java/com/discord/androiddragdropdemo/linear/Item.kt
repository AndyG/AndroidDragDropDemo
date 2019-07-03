package com.discord.androiddragdropdemo.linear

sealed class Item(open val id: Long) {

    data class Folder(val isOpen: Boolean, val numChildren: Int, override val id: Long) : Item(id)

    data class ColoredNumber(val number: Int, val color: Color, override val id: Long) : Item(id) {
        enum class Color {
            RED,
            BLUE,
            GREEN
        }
    }
}