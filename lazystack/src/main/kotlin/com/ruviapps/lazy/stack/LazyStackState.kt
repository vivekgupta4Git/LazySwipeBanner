package com.ruviapps.lazy.stack

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize

class LazyStackState(items: List<Any>) {
    val itemCount: Int = items.size
    var currentIndex by mutableIntStateOf(0)
        private set

    val swipeOffsetX by mutableStateOf(Animatable(0f))

    suspend fun rightSwipe(size: IntSize) {
        swipeOffsetX.animateTo(size.width.toFloat())
        currentIndex = wrapIndex(currentIndex - 1)
        swipeOffsetX.animateTo(0f)
    }

    suspend fun leftSwipe(size: IntSize) {
        swipeOffsetX.animateTo(-size.width.toFloat())
        currentIndex = wrapIndex(currentIndex + 1)
        swipeOffsetX.animateTo(0f)
    }

    private fun wrapIndex(index: Int): Int {
        return (index + itemCount) % itemCount
    }
}