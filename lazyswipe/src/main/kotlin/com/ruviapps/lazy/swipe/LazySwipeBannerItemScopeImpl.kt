package com.ruviapps.lazy.swipe

import androidx.compose.foundation.lazy.layout.MutableIntervalList

class LazySwipeBannerItemScopeImpl() : LazySwipeBannerItemScope {
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

