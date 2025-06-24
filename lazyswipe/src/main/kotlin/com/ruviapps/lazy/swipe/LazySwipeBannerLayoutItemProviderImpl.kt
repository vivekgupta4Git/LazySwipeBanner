package com.ruviapps.lazy.swipe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable

/**
 * An implementation of [LazyLayoutItemProvider] that provides item content, keys, and content types
 * for a custom stack-based LazyLayout.
 *
 * This item provider is responsible for:
 * - Mapping a global index to the corresponding interval and local index.
 * - Emitting the composable content at a given index via the provided interval content.
 * - Providing stable keys and content types for efficient recomposition.
 *
 * @param intervalContent The structured interval-based content provider which manages intervals
 *                        of stack layout items and associated metadata like keys and content types.
 *
 * @see LazySwipeBannerLayoutIntervalContent
 * @see LazyLayoutItemProvider
 */
@OptIn(ExperimentalFoundationApi::class)
class LazySwipeBannerLayoutItemProviderImpl(
    private val intervalContent: LazySwipeBannerLayoutIntervalContent<LazySwipeBannerLayoutInterval>,
    override val itemScope: LazySwipeBannerItemScope
    ) : LazySwipeBannerItemProvider {

    // Scope used to pass to each composable item
    private val stackScopeImpl = LazySwipeBannerItemScopeImpl()

    /**
     * Total number of items in the layout.
     */
    override val itemCount: Int
        get() = intervalContent.itemCount

    /**
     * Emits the composable at the given [index] using the appropriate interval's item content.
     *
     * @param index The global index of the item to be emitted.
     * @param key The key associated with the item (for Compose diffing).
     */
    @Composable
    override fun Item(index: Int, key: Any) {
        intervalContent.withInterval(index) { localIndex, content ->
            content.item(stackScopeImpl,localIndex)
        }
    }

    /**
     * Returns the stable key associated with the item at the given index.
     */
    override fun getKey(index: Int): Any = intervalContent.getKey(index)

    /**
     * Returns the content type of the item at the given index (used for recycling).
     */
    override fun getContentType(index: Int): Any? = intervalContent.getContentType(index)
}

@OptIn(ExperimentalFoundationApi::class)
internal interface LazySwipeBannerItemProvider : LazyLayoutItemProvider{
    val itemScope : LazySwipeBannerItemScope
}
