package com.discord.androiddragdropdemo.linear

data class ColoredNumber(val number: Int, val color: Color) {
    enum class Color {
        RED,
        BLUE,
        GREEN
    }
}
