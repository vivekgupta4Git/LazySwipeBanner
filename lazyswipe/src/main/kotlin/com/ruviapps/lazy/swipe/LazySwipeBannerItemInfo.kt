package com.ruviapps.lazy.swipe

/**
 * Contains useful information about an individual item in the LazySwipeBanner
 */
interface LazySwipeBannerItemInfo{
    /** The index of the item in the list */
    val index : Int

    /** The key of the item which was passed to the items() function */
    val key : Any

    /** The main axis offset of the item in pixels. It is relative to the start of the LazySwipeBanner Container. */
    val offset : Int

    /** The main axis size of the item in pixels **/
    val size: Int

    /** The content type of the item which was passed to the item() or items() function. */
    val contentType: Any?
        get() = null
}