package com.ruviapps.lazy.swipe

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.unit.IntSize

/**
 * Contains useful information about the currently displayed layout state of
 * the lazy banners.
 *
 * Use [LazySwipeBannerState.layoutInfo] to retrieve this
 */
interface LazySwipeBannerLayoutInfo{
    /**The list of [LazySwipeBannerItemInfo] representing all the currently visible items**/
    val visibleItemsInfo : List<LazySwipeBannerItemInfo>


    /**
     * The start offset of the layout's viewport in pixels. You can think of it as a minimum offset
     * which would be visible. Usually it is 0, but it can be negative if non-zero
     * [beforeContentPadding] was applied as the content displayed in the content padding area is
     * still visible.
     *
     * You can use it to understand what items from [visibleItemsInfo] are fully visible.
     */
    val viewportStartOffset: Int

    /**
     * The end offset of the layout's viewport in pixels. You can think of it as a maximum offset
     * which would be visible. It is the size of the lazy list layout minus [beforeContentPadding].
     *
     * You can use it to understand what items from [visibleItemsInfo] are fully visible.
     */
    val viewportEndOffset: Int

    /** The total count of items passed to [LazySwipeBanner] */
    val totalItemsCount: Int

    /**
     * The size of the viewport in pixels. It is the lazy list layout size including all the content
     * paddings.
     */
    val viewportSize: IntSize
        get() = IntSize.Zero

    /** The orientation of the lazy list. */
    val orientation: Orientation
        get() = Orientation.Vertical

    /** True if the direction of scrolling and layout is reversed. */
    val reverseLayout: Boolean
        get() = false

    /**
     * The content padding in pixels applied before the first item in the direction of scrolling.
     * For example it is a top content padding for LazyColumn with reverseLayout set to false.
     */
    val beforeContentPadding: Int
        get() = 0

    /**
     * The content padding in pixels applied after the last item in the direction of scrolling. For
     * example it is a bottom content padding for LazyColumn with reverseLayout set to false.
     */
    val afterContentPadding: Int
        get() = 0

    /** The spacing between items in the direction of scrolling. */
    val mainAxisItemSpacing: Int
        get() = 0
}