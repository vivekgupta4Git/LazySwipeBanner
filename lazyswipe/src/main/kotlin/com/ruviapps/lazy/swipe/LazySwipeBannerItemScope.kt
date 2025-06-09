package com.ruviapps.lazy.swipe

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import com.ruviapps.lazy.swipe.LazySwipeBannerItemAnimationConfig.Companion.RotationAxis
import kotlin.math.absoluteValue

/**
 * Represents a composable function type for items within a LazySwipeBanner.
 *
 * This type alias defines a lambda that receives a [LazySwipeBannerItemScope] and an item index,
 * and emits the corresponding composable content for that index.
 *
 * @see LazySwipeBannerItemScope
 */
internal typealias LazySwipeBannerLayoutComposable = @Composable (LazySwipeBannerItemScope.(Int) -> Unit)

/**
 * Scope for [items] content.
 */
interface LazySwipeBannerItemScope {
    /**
     * Adds a [count] of items.
     *
     * @param count the items count
     * @param key a factory of stable and unique keys representing the item. Using the same key for
     *   multiple items in the list is not allowed. Type of the key should be saveable via Bundle on
     *   Android. If null is passed the position in the list will represent the key.
     * @param contentType a factory of the content types for the item. The item compositions of the
     *   same type could be reused more efficiently. Note that null is a valid type and items of
     *   such type will be considered compatible.
     * @param itemContent the content displayed by a single item
     */
    fun items(
        count: Int,
        key: ((index: Int) -> Any)? = null,
        contentType: (index: Int) -> Any? = { null },
        itemContent: LazySwipeBannerLayoutComposable
    )

    /**
     * Applies an animation effect to a LazySwipeBanner item using the specified configuration.
     *
     * @receiver The Modifier to which the animation effect will be applied.
     * @param isCenterItem A Boolean indicating whether the item is the center item in the banner.
     *                     This affects the scale, alpha, and transform origin used in the animation.
     * @param state The current state of the LazySwipeBanner, which provides information such as
     *              swipe offset and orientation.
     * @param enableRotation A Boolean flag indicating whether rotation effects should be applied.
     *                       If true, the item will rotate based on the swipe offset.
     * @param config The configuration for the animation, including properties such as rotation
     *               divisor, camera distance, scale, alpha, and transform origins for center and
     *               side items.
     * @return A Modifier with the animation effect applied, based on the provided parameters and
     *         the current state of the LazySwipeBanner.
     */
    fun Modifier.lazySwipeBannerAnimatedItem(
        isCenterItem: Boolean,
        state: LazySwipeBannerState,
        enableRotation: Boolean = false,
        config: LazySwipeBannerItemAnimationConfig = LazySwipeBannerItemAnimationConfig.Default
    ): Modifier = this.then(
        Modifier.Companion.graphicsLayer {
            //Rotation based animation
            if (enableRotation) {
                val rotation = state.swipeOffset.value / config.rotationDivisor.coerceAtLeast(1f)
                when (config.rotationAxis) {
                    RotationAxis.X_AXIS -> this.rotationX = rotation
                    RotationAxis.Y_AXIS -> this.rotationY = rotation
                    RotationAxis.Z_AXIS -> this.rotationZ = rotation
                }
                this.cameraDistance = density.absoluteValue * config.cameraDistance
            } else {
                this.rotationY = 0f
                this.rotationZ = 0f
                this.rotationX = 0f
                this.cameraDistance = 0f
            }
            this.transformOrigin =
                if (isCenterItem) config.transformOriginCenter else config.transformOriginSide

            val scale = if (isCenterItem) config.scaleCenter else config.scaleSide
            val alpha = if (isCenterItem) config.alphaCenter else config.alphaSide
            this.scaleX = scale
            this.scaleY = scale
            this.alpha = alpha

            val centerItemTranslation = state.swipeOffset.value
            val sideItemTranslation = if (config.enablePeekAnimation)
                -centerItemTranslation / config.peekDuringAnimationDivisor.coerceAtLeast(1f)
            else
                0f

            if(config.enableTranslation){
                if (state.orientation == Orientation.Horizontal)
                    this.translationX = if (isCenterItem) centerItemTranslation else sideItemTranslation
                else
                    this.translationY =
                        if (isCenterItem) centerItemTranslation else sideItemTranslation
            }

        })
}

/**
 * Adds a list of items.
 *
 * @param items the data list
 * @param key a factory of stable and unique keys representing the item. Using the same key for
 *   multiple items in the list is not allowed. Type of the key should be saveable via Bundle on
 *   Android. If null is passed the position in the list will represent the key.
 * @param contentType a factory of the content types for the item. The item compositions of the same
 *   type could be reused more efficiently. Note that null is a valid type and items of such type
 *   will be considered compatible.
 * @param itemContent the content displayed by a single item
 */
inline fun <T> LazySwipeBannerItemScope.items(
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable (LazySwipeBannerItemScope.(T) -> Unit)
) = items(
    count = items.size,
    key = if (key != null) { index: Int -> key(items[index]) } else null,
    contentType = { index: Int -> contentType(items[index]) },
) {
    itemContent(items[it])
}

/**
 * Adds a list of items where the content of an item is aware of its index.
 *
 * @param items the data list
 * @param key a factory of stable and unique keys representing the item. Using the same key for
 *   multiple items in the list is not allowed. Type of the key should be saveable via Bundle on
 *   Android. If null is passed the position in the list will represent the key.
 * @param contentType a factory of the content types for the item. The item compositions of the same
 *   type could be reused more efficiently. Note that null is a valid type and items of such type
 *   will be considered compatible.
 * @param itemContent the content displayed by a single item
 */
inline fun <T> LazySwipeBannerItemScope.itemsIndexed(
    items: List<T>,
    noinline key: ((index: Int, item: T) -> Any)? = null,
    crossinline contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable LazySwipeBannerItemScope.(index: Int, item: T) -> Unit,
) =
    items(
        count = items.size,
        key = if (key != null) { index: Int -> key(index, items[index]) } else null,
        contentType = { index -> contentType(index, items[index]) },
    ) {
        itemContent(it, items[it])
    }

/**
 * Adds an array of items.
 *
 * @param items the data array
 * @param key a factory of stable and unique keys representing the item. Using the same key for
 *   multiple items in the list is not allowed. Type of the key should be saveable via Bundle on
 *   Android. If null is passed the position in the list will represent the key
 * @param contentType a factory of the content types for the item. The item compositions of the same
 *   type could be reused more efficiently. Note that null is a valid type and items of such type
 *   will be considered compatible.
 * @param itemContent the content displayed by a single item
 */
inline fun <T> LazySwipeBannerItemScope.items(
    items: Array<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazySwipeBannerItemScope.(item: T) -> Unit,
) =
    items(
        count = items.size,
        key = if (key != null) { index: Int -> key(items[index]) } else null,
        contentType = { index: Int -> contentType(items[index]) },
    ) {
        itemContent(items[it])
    }