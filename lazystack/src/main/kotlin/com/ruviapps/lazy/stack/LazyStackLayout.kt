package com.ruviapps.lazy.stack

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyStackLayout(
    modifier: Modifier = Modifier,
    state: LazyStackState,
    itemOffset: Dp = 50.dp,
    key: ((index: Int) -> Any)? = null,
    itemContent: LazyStackItemScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val stackItemProvider = rememberStackItemProviderLambda(
        state = state,
        content = itemContent,
        key = key
    )
    LazyLayout(
        itemProvider = stackItemProvider, modifier.pointerInput(state.currentIndex) {
            detectDragGestures(
                onDragEnd = {
                    val threshold = size.width / 3f
                    when {
                        state.swipeOffset.value > threshold -> {
                            scope.launch {
                                state.rightSwipe(size)
                            }
                        }

                        state.swipeOffset.value < -threshold -> {
                            scope.launch {
                                state.leftSwipe(size)
                            }
                        }

                        else -> {
                            // Not enough swipe, reset
                            scope.launch {
                                state.swipeOffset.animateTo(0f)
                            }
                        }
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val dragValue = if(state.orientation == Orientation.Vertical) dragAmount.y else dragAmount.x
                    scope.launch {
                        state.swipeOffset.snapTo(state.swipeOffset.value + dragValue)
                    }
                }
            )
        },
        null
    ) { constraints ->
        val offsetValue = with(density) {
            itemOffset.toPx().roundToInt()
        }
        val itemConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
            maxWidth = constraints.maxWidth,
            maxHeight = constraints.maxHeight

        )
        val itemsToMeasure = listOf(
            state.currentIndex,
            (state.currentIndex + 1 + state.itemCount) % state.itemCount,
            (state.currentIndex - 1 + state.itemCount) % state.itemCount
        )
        val placeables = mutableListOf<Placeable>()
        itemsToMeasure.forEach { index ->
            val placeable = measure(index, itemConstraints)
            placeables.addAll(placeable)
        }
        val maxHeight = placeables.maxOf { it.height }
        val maxWidth = placeables.maxOf { it.width }
        val constraintWidth = maxWidth + offsetValue * 2
        layout(constraintWidth, maxHeight) {

            placeables.forEachIndexed { index, placeable ->
                when (index) {
                    0 -> {
                        //current
                        val x0 = (constraintWidth - placeable.width) / 2
                        val y0 = (maxHeight - placeable.height) / 2
                        placeable.placeRelative(
                            x0,
                            y0,
                            2f
                        )
                    }

                    1 -> {
                        //next
                        val x1 = ((constraintWidth - placeable.width) / 2) + offsetValue
                        val y1 = (maxHeight - placeable.height) / 2
                        placeable.placeRelative(x1, y1)
                    }

                    2 -> {
                        //previous
                        val x2 = ((constraintWidth - placeable.width) / 2) - offsetValue
                        val y2 = (maxHeight - placeable.height) / 2
                        placeable.placeRelative(x2, y2)
                    }

                }
            }
        }
    }
}


