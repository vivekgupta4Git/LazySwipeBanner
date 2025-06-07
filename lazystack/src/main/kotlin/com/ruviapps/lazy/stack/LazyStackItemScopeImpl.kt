package com.ruviapps.lazy.stack

import androidx.compose.foundation.lazy.layout.IntervalList
import androidx.compose.foundation.lazy.layout.MutableIntervalList

class LazyStackItemScopeImpl() : LazyStackItemScope {
    private val _intervalList = MutableIntervalList<StackLazyLayoutInterval>()
   // val intervalList : IntervalList<StackLazyLayoutInterval> = _intervalList
    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        itemContent: LazyStackLayoutComposable
    ) {
       _intervalList.addInterval(
            count,
            StackLazyLayoutInterval(
                key = key,
                item = itemContent,
                type = contentType
            )
        )
    }
}

