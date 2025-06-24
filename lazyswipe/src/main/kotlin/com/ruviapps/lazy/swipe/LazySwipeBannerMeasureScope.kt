package com.ruviapps.lazy.swipe

import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureScope

@Stable
sealed interface LazySwipeBannerMeasureScope : MeasureScope {

    /**
     * Compose an item of lazy layout.
     *
     * @param index the item index. Should be no larger that [LazySwipeBannerLayoutItemProviderImpl.itemCount].
     * @return List of [androidx.compose.ui.layout.Measurable]s
     */
    fun compose( index: Int): List<Measurable>
}