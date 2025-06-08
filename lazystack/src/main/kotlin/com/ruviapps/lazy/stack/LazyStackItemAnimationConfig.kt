package com.ruviapps.lazy.stack

import androidx.compose.ui.graphics.TransformOrigin

data class LazyStackItemAnimationConfig(
    val scaleCenter: Float = 1f,
    val alphaCenter: Float = 1f,
    val scaleSide: Float = 0.8f,
    val alphaSide: Float = 0.8f,
    val cameraDistance: Float = 8f,
    val transformOrigin: TransformOrigin = TransformOrigin.Companion.Center,
    val rotationDivisor: Float = 10f
) {
    companion object {
        val Default = LazyStackItemAnimationConfig()
    }
}