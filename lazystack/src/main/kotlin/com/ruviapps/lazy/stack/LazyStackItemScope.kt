package com.ruviapps.lazy.stack

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

typealias LazyStackLayoutComposable = @Composable (LazyStackItemScope.(Int) -> Unit)

/**
 * Scope for [items] content.
 */
interface LazyStackItemScope {
    /**
     * Adds a [count] of items.
     *
     * @param count the items count
     * @param key a factory of stable and unique keys representing the item. Using the same key for
     *   multiple items in the list is not allowed. Type of the key should be saveable via Bundle on
     *   Android. If null is passed the position in the list will represent the key. When you
     *   specify the key the scroll position will be maintained based on the key, which means if you
     *   add/remove items before the current visible item the item with the given key will be kept
     *   as the first visible one. This can be overridden by calling 'requestScrollToItem' on the
     *   'LazyStackState'.
     * @param contentType a factory of the content types for the item. The item compositions of the
     *   same type could be reused more efficiently. Note that null is a valid type and items of
     *   such type will be considered compatible.
     * @param itemContent the content displayed by a single item
     */
    fun items(
        count: Int,
        key: ((index: Int) -> Any)? = null,
        contentType: (index: Int) -> Any? = { null },
        itemContent: LazyStackLayoutComposable
    )
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
        })

}

/**
 * Adds a list of items.
 *
 * @param items the data list
 * @param key a factory of stable and unique keys representing the item. Using the same key for
 *   multiple items in the list is not allowed. Type of the key should be saveable via Bundle on
 *   Android. If null is passed the position in the list will represent the key. When you specify
 *   the key the scroll position will be maintained based on the key, which means if you add/remove
 *   items before the current visible item the item with the given key will be kept as the first
 *   visible one. This can be overridden by calling 'requestScrollToItem' on the 'LazyStackState'.
 * @param contentType a factory of the content types for the item. The item compositions of the same
 *   type could be reused more efficiently. Note that null is a valid type and items of such type
 *   will be considered compatible.
 * @param itemContent the content displayed by a single item
 */
inline fun <T> LazyStackItemScope.items(
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable (LazyStackItemScope.(T) -> Unit)
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
 *   Android. If null is passed the position in the list will represent the key. When you specify
 *   the key the scroll position will be maintained based on the key, which means if you add/remove
 *   items before the current visible item the item with the given key will be kept as the first
 *   visible one. This can be overridden by calling 'requestScrollToItem' on the 'LazyStackState'.
 * @param contentType a factory of the content types for the item. The item compositions of the same
 *   type could be reused more efficiently. Note that null is a valid type and items of such type
 *   will be considered compatible.
 * @param itemContent the content displayed by a single item
 */
inline fun <T> LazyStackItemScope.itemsIndexed(
    items: List<T>,
    noinline key: ((index: Int, item: T) -> Any)? = null,
    crossinline contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable LazyStackItemScope.(index: Int, item: T) -> Unit,
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
 *   Android. If null is passed the position in the list will represent the key. When you specify
 *   the key the scroll position will be maintained based on the key, which means if you add/remove
 *   items before the current visible item the item with the given key will be kept as the first
 *   visible one. This can be overridden by calling 'requestScrollToItem' on the 'LazyStackState'.
 * @param contentType a factory of the content types for the item. The item compositions of the same
 *   type could be reused more efficiently. Note that null is a valid type and items of such type
 *   will be considered compatible.
 * @param itemContent the content displayed by a single item
 */
inline fun <T> LazyStackItemScope.items(
    items: Array<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyStackItemScope.(item: T) -> Unit,
) =
    items(
        count = items.size,
        key = if (key != null) { index: Int -> key(items[index]) } else null,
        contentType = { index: Int -> contentType(items[index]) },
    ) {
        itemContent(items[it])
    }