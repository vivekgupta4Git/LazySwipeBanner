package com.ruviapps.lazy.swipe

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope

/** The result of the measure pass for the Lazy Swipe Banner Layout**/
internal class LazySwipeBannerMeasureResult(
    val firstVisibleItem : LazySwipeBannerMeasuredItem?,
    val firstVisibleItemScrollOffset : Int,
    val canScrollForward : Boolean,
    val consumedScroll : Float,
    private val measuredResult : MeasureResult,
    val scrollBackAmount : Float,
    val remeasuredNeeded : Boolean,
    val coroutineScope: CoroutineScope,
    val density : Density,
    val childConstraints : Constraints,
    override val visibleItemsInfo: List<LazySwipeBannerItemInfo>,
    override val viewportStartOffset: Int,
    override val viewportEndOffset: Int,
    override val totalItemsCount: Int,
    override val reverseLayout: Boolean,
    override val orientation: Orientation,
    override val afterContentPadding: Int,
    override val mainAxisItemSpacing: Int
) : LazySwipeBannerLayoutInfo, MeasureResult by measuredResult{

    val canScrollBackWard
        get() = (firstVisibleItem?.index ?: 0 ) !=0 || firstVisibleItemScrollOffset != 0

    override val viewportSize: IntSize
        get() = IntSize(width,height)

    override val beforeContentPadding : Int
        get() = -viewportStartOffset
}