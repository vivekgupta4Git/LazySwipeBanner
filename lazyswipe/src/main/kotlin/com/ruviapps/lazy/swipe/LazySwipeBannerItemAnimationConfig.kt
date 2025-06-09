package com.ruviapps.lazy.swipe

import androidx.compose.ui.graphics.TransformOrigin


/**
 * Configuration for animation of items within a LazySwipeBanner.
 *
 * @param enableTranslation Whether item translation animation should be enabled. Default is true.
 * @param scaleCenter The scale applied to the center item. Default is 1f.
 * @param alphaCenter The alpha applied to the center item. Default is 1f.
 * @param scaleSide The scale applied to side items. Default is 0.8f.
 * @param alphaSide The alpha applied to side items. Default is 0.8f.
 * @param cameraDistance The camera distance used for side item rotations. Default is 2.5f.
 * @param transformOriginCenter The transform origin used for center item rotations. Default is [TransformOrigin.Center].
 * @param transformOriginSide The transform origin used for side item rotations. Default is [TransformOrigin.Center].
 * @param rotationDivisor The divisor used to calculate the rotation applied to side items. Default is 10f.
 * @param rotationAxis The axis of rotation used for side items. Default is [RotationAxis.Y_AXIS].
 * @param enablePeekAnimation Whether the peek animation should be enabled. Default is true.
 * @param peekDuringAnimationDivisor The divisor used to calculate the peek amount during animation. Default is 10f.
 */
data class LazySwipeBannerItemAnimationConfig(
    val enableTranslation : Boolean = true,
    val scaleCenter: Float = 1f,
    val alphaCenter: Float = 1f,
    val scaleSide: Float = 0.8f,
    val alphaSide: Float = 0.8f,
    val cameraDistance: Float = 2.5f,
    val transformOriginCenter: TransformOrigin = TransformOrigin.Center,
    val transformOriginSide : TransformOrigin = TransformOrigin.Center,
    val rotationDivisor: Float = 10f,
    val rotationAxis : RotationAxis = RotationAxis.Y_AXIS,
    val enablePeekAnimation : Boolean = true,
    val peekDuringAnimationDivisor : Float= 10f
) {
    companion object {
        val Default = LazySwipeBannerItemAnimationConfig()

        /**
         * Configuration for a Vertical flip animation (use it by enabling rotation on [LazySwipeBannerItemScope.lazySwipeBannerAnimatedItem])
         *
         * Note : This configuration disable translation, to enable translation
         * override this configuration
         */
        val VerticalFlip = Default.copy(
            rotationAxis = RotationAxis.X_AXIS,
            rotationDivisor = 8f,
            cameraDistance = 8f,
            transformOriginCenter = TransformOrigin(0.5f,0.5f),
            enablePeekAnimation = false,
            enableTranslation = false
        )

        /**
         * Configuration for a Horizontal flip animation (use it by enabling rotation on [LazySwipeBannerItemScope.lazySwipeBannerAnimatedItem])
         *
         * Note : This configuration disable translation, to enable translation
         * override this configuration
         */
        val HorizontalFlip = LazySwipeBannerItemAnimationConfig.Default.copy(
            rotationAxis = RotationAxis.Y_AXIS,
            rotationDivisor = 8f,
            cameraDistance = 8f,
            transformOriginCenter = TransformOrigin(0.5f,0.5f),
            enablePeekAnimation = false,
            enableTranslation = false
        )
        /**
         *  Configuration for a Horizontal flip animation along with swipe (use it by enabling rotation on [LazySwipeBannerItemScope.lazySwipeBannerAnimatedItem])
         */
        val HorizontalFlipAndSwipe = Default.copy(
            rotationAxis = RotationAxis.Y_AXIS,
            rotationDivisor = 5f,
            transformOriginCenter = TransformOrigin(0.5f,0f),
            cameraDistance = 8f,
            enablePeekAnimation = false,
        )
        /**
         *  Configuration for a Vertical flip animation along with swipe (use it by enabling rotation on [LazySwipeBannerItemScope.lazySwipeBannerAnimatedItem])
         */
        val VerticalFlipAndSwipe = Default.copy(
            rotationAxis = RotationAxis.X_AXIS,
            rotationDivisor = 5f,
            transformOriginCenter = TransformOrigin(0.5f,0f),
            cameraDistance = 8f,
            enablePeekAnimation = false,
        )

        enum class RotationAxis{
            X_AXIS,
            Y_AXIS,
            Z_AXIS
        }
    }
}
