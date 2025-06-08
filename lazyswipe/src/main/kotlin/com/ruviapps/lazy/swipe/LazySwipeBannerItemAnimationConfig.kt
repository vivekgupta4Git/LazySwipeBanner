package com.ruviapps.lazy.swipe

import androidx.compose.ui.graphics.TransformOrigin

data class LazySwipeBannerItemAnimationConfig(
    val scaleCenter: Float = 1f,
    val alphaCenter: Float = 1f,
    val scaleSide: Float = 0.8f,
    val alphaSide: Float = 0.8f,
    val cameraDistance: Float = 2.5f,
    val transformOriginCenter: TransformOrigin = TransformOrigin.Center,
    val transformOriginSide : TransformOrigin = TransformOrigin.Center,
    val rotationDivisor: Float = 10f,
    val enablePeekAnimation : Boolean = true,
    val peekDuringAnimationDivisor : Float= 10f
) {
    companion object {
        val Default = LazySwipeBannerItemAnimationConfig()
    }
}