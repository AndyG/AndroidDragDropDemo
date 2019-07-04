package com.discord.androiddragdropdemo.app

import android.app.Application
import com.discord.androiddragdropdemo.repository.NumbersRepository

@Suppress("unused")
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NumbersRepository.init()
    }
}