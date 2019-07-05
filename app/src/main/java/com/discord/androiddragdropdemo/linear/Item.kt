package com.discord.androiddragdropdemo.linear

import com.discord.androiddragdropdemo.domain.ColoredNumber

sealed class Item(open val id: Long) {

    data class PlaceholderListItem(val folderId: Long?) : Item(PLACEHOLDER_ID)

    data class FolderListItem(
        val isOpen: Boolean,
        val numChildren: Int,
        val isTargeted: Boolean,
        override val id: Long) : Item(id)

    data class ColoredNumberListItem(val coloredNumber: ColoredNumber,
                                     val folderId: Long?,
                                     val isTargeted: Boolean = false) : Item(id = coloredNumber.id)

    companion object {
        const val PLACEHOLDER_ID = -1L
    }
}