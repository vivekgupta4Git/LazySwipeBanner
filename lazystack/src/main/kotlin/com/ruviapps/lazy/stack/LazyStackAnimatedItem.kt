package com.ruviapps.lazy.stack

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
            if (enableRotation) state.swipeOffsetX.value / config.rotationDivisor.coerceAtLeast(
                1f
            ) else 0f
        this.rotationY = rotationY

        this.cameraDistance =
            if (enableRotation) density.absoluteValue * config.cameraDistance else 0f

        this.transformOrigin = config.transformOrigin

        val scale = if (isCenterItem) config.scaleCenter else config.scaleSide
        val alpha = if (isCenterItem) config.alphaCenter else config.alphaSide
        val translationX = state.swipeOffsetX.value

        this.scaleX = scale
        this.scaleY = scale
        this.alpha = alpha
        this.translationX = if (isCenterItem) translationX else -translationX / 10
    }
)