package com.ruviapps.lazy.stack

import androidx.compose.foundation.lazy.layout.IntervalList
import androidx.compose.foundation.lazy.layout.MutableIntervalList

/**
 * A concrete implementation of [StackLazyLayoutIntervalContent] that provides a list of items
 * to be displayed in a custom LazyStack layout.
 *
 * This class wraps a composable content function and associates it with a key factory and a total
 * number of items (size) to be displayed as a single interval.
 *
 * @property itemContent A composable lambda that generates the UI for each item in the interval.
 *                       It takes a [LazyStackItemScope] and the local item index, and emits
 *                       the corresponding composable.
 *
 * @property key An optional function that generates a stable key for each item based on its index.
 *               Stable keys improve Compose’s ability to efficiently track item state across
 *               recompositions.
 *
 * @property size The number of items in this interval (i.e., how many total pages/items
 *                     this content block represents).
 *
 * @constructor Initializes the [intervals] list with a single interval of size [size],
 *              backed by [itemContent] and optionally [key].
 *
 * @see StackLazyLayoutInterval
 * @see StackLazyLayoutIntervalContent
 */
class StackLayoutIntervalContent(
    val itemContent: LazyStackLayoutComposable,
    val key: ((Int) -> Any)?,
    val size: Int
) : StackLazyLayoutIntervalContent<StackLazyLayoutInterval>() {
    override val intervals: IntervalList<StackLazyLayoutInterval> =
        MutableIntervalList<StackLazyLayoutInterval>()
            .apply {
                addInterval(this@StackLayoutIntervalContent.size, StackLazyLayoutInterval(key = key, item = itemContent))
            }
}