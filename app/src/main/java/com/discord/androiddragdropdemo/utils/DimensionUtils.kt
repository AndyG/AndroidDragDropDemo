package com.discord.androiddragdropdemo.utils

import android.content.res.Resources
import android.util.TypedValue

private fun dpToPx(dp: Int, resources: Resources): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    )
}

