package com.ruviapps.lazy.swipe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A composable function that displays a stack of items that can be swiped through.
 *
 * This composable displays a stack of items, with the currently selected item in the center.
 * The user can swipe left or right to switch to the next or previous item.
 *
 * @param modifier The modifier to apply to this component.
 * @param state The state of the stack. This state must be remembered and passed to this composable.
 * @param itemOffset The offset between each item in the stack. Defaults to 50.dp.
 * @param key A lambda that generates a stable key for each item based on its index. This key is
 * used to help Compose track the state of each item. Defaults to null.
 * @param itemContent A lambda that generates the content for each item. This lambda is expected to
 * accept a [LazySwipeBannerItemScope] and an integer index, and produce the corresponding UI
 * content for that index.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazySwipeBanner(
    modifier: Modifier = Modifier,
    state: LazySwipeBannerState,
    itemOffset: Dp = 50.dp,
    key: ((index: Int) -> Any)? = null,
    itemContent: LazySwipeBannerItemScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val stackItemProvider = rememberLazySwipeBannerItemProviderLambda(
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
                                state.decreaseIndex(size)
                            }
                        }

                        state.swipeOffset.value < -threshold -> {
                            scope.launch {
                                state.increaseIndex(size)
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
                    val dragValue =
                        if (state.orientation == Orientation.Vertical) dragAmount.y else dragAmount.x
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
        val constraintHeight = if (state.orientation == Orientation.Vertical)
            maxHeight + offsetValue * 2
        else
            maxHeight
        val constraintWidth = if (state.orientation == Orientation.Vertical)
            maxWidth
        else
            maxWidth + offsetValue * 2



        layout(constraintWidth, constraintHeight) {

            placeables.forEachIndexed { index, placeable ->
                val baseX = (constraintWidth - placeable.width) / 2
                val baseY = (constraintHeight - placeable.height) / 2
                when (index) {
                    0 -> {
                        //center
                        placeable.placeRelative(
                            baseX,
                            baseY,
                            1f
                        )
                    }

                    1 -> {
                        //next
                        val (x, y) = if (state.orientation == Orientation.Vertical) {
                            // When Vertical:
                            // x = baseX
                            // y = baseY + offsetValue
                            Pair(baseX, baseY + offsetValue)
                        } else {
                            // When Horizontal :
                            // x = baseX + offsetValue
                            // y = baseY
                            Pair(baseX + offsetValue, baseY)
                        }

                        placeable.placeRelative(x, y, 0f)
                    }

                    2 -> {
                        //previous
                        val (x, y) = if (state.orientation == Orientation.Vertical) {
                            // When Vertical:
                            // x = baseX
                            // y = baseY - offsetValue
                            Pair(baseX, baseY - offsetValue)
                        } else {
                            // When Horizontal :
                            // x = baseX - offsetValue
                            // y = baseY
                            Pair(baseX - offsetValue, baseY)
                        }

                        placeable.placeRelative(x, y, 0f)
                    }

                }
            }
        }
    }
}


