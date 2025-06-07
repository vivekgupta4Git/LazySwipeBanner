package com.ruviapps.lazy.stack

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize

class LazyStackState(
    val itemCount: Int = 3,
    initialIndex: Int = 0
) {

    var currentIndex by mutableIntStateOf(initialIndex)
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

    companion object {
        fun Saver(itemCount: Int): Saver<LazyStackState, *> =
            Saver(
                save = { it.currentIndex },
                restore = { savedIndex -> LazyStackState(itemCount, savedIndex) }
            )
    }
}

@Composable
fun rememberLazyStackState(itemCount: Int = 3): LazyStackState {
    return rememberSaveable(saver = LazyStackState.Saver(itemCount)) {
        LazyStackState(itemCount)
    }
}

