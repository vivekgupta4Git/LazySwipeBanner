package com.ruviapps.lazy.stack

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

fun Modifier.lazyStackAnimatedItem(
    isCenterItem: Boolean,
    state: LazyStackState,
    enableRotation: Boolean = false,
    config: LazyStackItemAnimationConfig = LazyStackItemAnimationConfig.Default
): Modifier = this.then(
    Modifier.Companion.graphicsLayer {
        val rotationY =
            if (enableRotation) state.swipeOffset.value / config.rotationDivisor.coerceAtLeast(
                1f
            ) else 0f
        this.rotationY = rotationY

        this.cameraDistance =
            if (enableRotation) density.absoluteValue * config.cameraDistance else 0f

        if (enableRotation)
            this.transformOrigin =
                if (isCenterItem) config.transformOriginCenter else config.transformOriginSide

        val scale = if (isCenterItem) config.scaleCenter else config.scaleSide
        val alpha = if (isCenterItem) config.alphaCenter else config.alphaSide
        val translation = state.swipeOffset.value

        this.scaleX = scale
        this.scaleY = scale
        this.alpha = alpha
        if (state.orientation == Orientation.Horizontal)
            this.translationX = if (isCenterItem) translation else
                if (config.enablePeekAnimation)
                    -translation / config.peekDuringAnimationDivisor.coerceAtLeast(1f)
                else
                    0f
        else
            this.translationY =
                if (isCenterItem) translation else
                    if (config.enablePeekAnimation)
                        -translation / config.peekDuringAnimationDivisor.coerceAtLeast(1f)
                    else
                        0f
    }
)