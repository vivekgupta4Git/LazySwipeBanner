package com.ruviapps.lazy.stack

import androidx.compose.foundation.ExperimentalFoundationApi
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
    offSetItemScale: Float = 0.8f,
    offsetAlpha: Float = 0.8f,
    itemContent: LazyStackItemScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val stackItemProvider = rememberLazyStackItemProvider(key1 = state.currentIndex, content = itemContent)

    LazyLayout(
        { stackItemProvider }, modifier.pointerInput(state.currentIndex) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    val threshold = size.width / 3f
                    when {
                        state.swipeOffsetX.value > threshold -> {
                            scope.launch {
                                state.rightSwipe(size)
                            }
                        }

                        state.swipeOffsetX.value < -threshold -> {
                            scope.launch {
                                state.leftSwipe(size)
                            }
                        }

                        else -> {
                            // Not enough swipe, reset
                            scope.launch {
                                state.swipeOffsetX.animateTo(0f)
                            }
                        }
                    }
                },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        state.swipeOffsetX.snapTo(state.swipeOffsetX.value + dragAmount)
                    }
                }
            )
        },
        null
    ) { constraints ->
        val offsetValue = with(density) {
            itemOffset.toPx().roundToInt()
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val itemsToMeasure = listOf(
                state.currentIndex,
                (state.currentIndex + 1 + state.itemCount) % state.itemCount,
                (state.currentIndex - 1 + state.itemCount) % state.itemCount
            )
            val placeables = mutableListOf<Placeable>()
            itemsToMeasure.forEach { index ->
                val placeable = measure(index, constraints)
                placeables.addAll(placeable)
            }
            val x0 = (constraints.maxWidth - placeables[0].width) / 2
            val y0 = (constraints.maxHeight - placeables[0].height) / 2
            val centeredItem = placeables[0]
            val rightItem = placeables[1]
            val leftItem = placeables[2]

            centeredItem.placeRelativeWithLayer(
                x0,
                y0,
                2f
            ) {
               // transformOrigin = TransformOrigin(0.5f, 0.5f)
               // rotationY = state.swipeOffsetX.value / 10
                alpha = 1f
                scaleY = 1f
                this.translationX = state.swipeOffsetX.value
            }

            val x1 = ((constraints.maxWidth - placeables[1].width) / 2) + offsetValue
            val y1 = (constraints.maxHeight - placeables[1].height) / 2

            rightItem.placeRelativeWithLayer(x1, y1, 1f) {
               // transformOrigin = TransformOrigin(0.5f, 0.5f)
               // rotationY = state.swipeOffsetX.value / 10
               // cameraDistance = density.absoluteValue * 2.5f
                // shadowElevation = state.swipeOffsetX.value/10
                alpha = offsetAlpha
                scaleY = offSetItemScale
                //this.translationX = -state.swipeOffsetX.value / 10f
                //val shadowAlpha = 1f - abs(rotationY / 90f)
                // shadowElevation = 8.dp.toPx() * shadowAlpha
            }


            val x2 = ((constraints.maxWidth - placeables[2].width) / 2) - offsetValue
            val y2 = (constraints.maxHeight - placeables[2].height) / 2

            leftItem.placeRelativeWithLayer(x2, y2, 1f) {
              //  transformOrigin = TransformOrigin(0.5f, 0.5f)
              //  rotationY = state.swipeOffsetX.value / 10
                alpha = offsetAlpha
                scaleY = offSetItemScale
               // this.translationX = -state.swipeOffsetX.value / 10f
            //    cameraDistance = density.absoluteValue * 2.5f
                // val shadowAlpha = 1f - abs(rotationY / 90f)
                // shadowElevation = 8.dp.toPx() * shadowAlpha

                //this.translationY = -state.swipeOffsetX.value.coerceAtLeast(0f)
                // shadowElevation = state.swipeOffsetX.value/10
            }
        }
    }

}