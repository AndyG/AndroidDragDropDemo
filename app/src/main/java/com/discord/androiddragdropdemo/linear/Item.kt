package com.discord.androiddragdropdemo.linear

import com.discord.androiddragdropdemo.domain.ColoredNumber

sealed class Item(open val id: Long) {

    object PlaceholderListItem : Item(-1L)

    data class FolderListItem(val isOpen: Boolean, val numChildren: Int, override val id: Long) : Item(id)

    data class ColoredNumberListItem(val coloredNumber: ColoredNumber, val isTargeted: Boolean = false) : Item(id = coloredNumber.id)
}