package com.ruviapps.lazy.swipe

import androidx.compose.foundation.lazy.layout.MutableIntervalList

/**
 * Implementation of [LazySwipeBannerItemScope] that manages a list of interval content for a lazy banner layout.
 *
 * This class is responsible for creating and maintaining intervals of items to be displayed within
 * a custom LazyStack layout. Each interval is represented by a [LazySwipeBannerLayoutInterval], which
 * includes a key generator, content type provider, and composable content for each item.
 *
 * @see LazySwipeBannerItemScope
 * @see LazySwipeBannerLayoutInterval
 */
internal class LazySwipeBannerItemScopeImpl() : LazySwipeBannerItemScope {

    private val _intervalList = MutableIntervalList<LazySwipeBannerLayoutInterval>()
   // val intervalList : IntervalList<StackLazyLayoutInterval> = _intervalList
    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        itemContent: LazySwipeBannerLayoutComposable
    ) {
       _intervalList.addInterval(
            count,
            LazySwipeBannerLayoutInterval(
                key = key,
                item = itemContent,
                type = contentType
            )
        )
    }
}

