package com.discord.androiddragdropdemo.domain

data class ColoredNumber(val id: Long, val number: Int, val color: Color) {
    enum class Color {
        RED,
        BLUE,
        GREEN
    }
}