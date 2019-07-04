package com.discord.androiddragdropdemo.utils

import kotlin.random.Random

private val generatedIds = HashSet<Long>()

fun generateId(): Long {
    val id = Random.nextLong()
    if (generatedIds.contains(id)) {
        return generateId()
    }

    generatedIds.add(id)
    return id
}