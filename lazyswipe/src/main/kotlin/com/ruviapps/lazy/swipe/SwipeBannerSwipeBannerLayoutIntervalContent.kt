package com.ruviapps.lazy.swipe

import androidx.compose.foundation.lazy.layout.MutableIntervalList

/**
 * A concrete implementation of [LazySwipeBannerLayoutIntervalContent] that provides a list of items
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
 * @see LazySwipeBannerLayoutInterval
 * @see LazySwipeBannerLayoutIntervalContent
 */
class SwipeBannerSwipeBannerLayoutIntervalContent(
    val itemContent: LazySwipeBannerItemScope.()-> Unit,
    val key: ((Int) -> Any)?,
    val size: Int
) : LazySwipeBannerLayoutIntervalContent<LazySwipeBannerLayoutInterval>(), LazySwipeBannerItemScope {
    override val intervals: MutableIntervalList<LazySwipeBannerLayoutInterval> = MutableIntervalList<LazySwipeBannerLayoutInterval>()

    init {
        apply(itemContent)
    }

    override fun items(
        count: Int,
        key: ((Int) -> Any)?,
        contentType: (Int) -> Any?,
        itemContent: LazySwipeBannerLayoutComposable
    ) {
        intervals.addInterval(
            count,
            LazySwipeBannerLayoutInterval(
                key = key,
                type = contentType,
                item = itemContent
            )
        )
    }
}