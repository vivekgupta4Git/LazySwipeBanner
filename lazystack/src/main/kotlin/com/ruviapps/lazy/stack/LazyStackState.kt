package com.ruviapps.lazy.stack

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
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
    initialIndex: Int = 0,
    val orientation: Orientation = Orientation.Horizontal
) {
    init {
        require(itemCount >= 3) {
            error("Lazy Stack requires at least 3 items.")
        }
    }

    var currentIndex by mutableIntStateOf(initialIndex)
        private set

    val swipeOffset by mutableStateOf(Animatable(0f))

    suspend fun decreaseIndex(size: IntSize) {
        val (dimension, index) = if (orientation == Orientation.Vertical)
            Pair(size.height.toFloat(), currentIndex - 1)
        else
            Pair(size.width.toFloat(), currentIndex - 1)


        currentIndex = wrapIndex(index)
        //snap to opposite direction so that new card reveal looks more natural
        swipeOffset.snapTo(-dimension)
        //once it snapped to opposite direction bring it to the center again.
        swipeOffset.animateTo(0f)
    }

    suspend fun increaseIndex(size: IntSize) {
        val (dimension, index) = if (orientation == Orientation.Vertical)
            Pair(size.height.toFloat(), currentIndex + 1)
        else
            Pair(size.width.toFloat(), currentIndex + 1)  //  swipeOffset.animateTo(-dimension)
        currentIndex = wrapIndex(index)
        //snap to opposite direction so that new card reveal looks more natural
        swipeOffset.snapTo(dimension)
        //once it snapped to opposite direction bring it to the center again.
        swipeOffset.animateTo(0f)
    }

    private fun wrapIndex(index: Int): Int {
        return (index + itemCount) % itemCount
    }

    companion object {
        fun Saver(itemCount: Int): Saver<LazyStackState, *> =
            Saver(
                save = {
                    listOf(it.currentIndex, it.orientation.ordinal)
                },
                restore = { list ->
                    val saveIndex = list[0]
                    val orientation = Orientation.entries.toTypedArray().getOrElse(list[1]) {
                        Orientation.Horizontal
                    }
                    LazyStackState(
                        itemCount = itemCount,
                        initialIndex = saveIndex,
                        orientation = orientation
                    )

                }
            )
    }
}

@Composable
fun rememberLazyStackState(
    itemCount: Int = 3,
    initialIndex: Int = 0,
    orientation: Orientation = Orientation.Horizontal
): LazyStackState {
    return rememberSaveable(saver = LazyStackState.Saver(itemCount), key = "$orientation") {
        LazyStackState(itemCount, initialIndex, orientation)
    }
}

