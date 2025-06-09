package com.ruviapps.lazy.swipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Remembers and returns a stable lambda that provides a [LazySwipeBannerLayoutItemProviderImpl],
 * backed by the current state, key generation logic, and content composable.
 *
 * This function ensures that recompositions of [LazySwipeBanner] will efficiently reuse
 * the item provider unless relevant dependencies change.
 *
 * Internally, this constructs a single interval of items using [SwipeBannerSwipeBannerLayoutIntervalContent],
 * based on the current [LazySwipeBannerState.itemCount], and wraps it with a [LazySwipeBannerLayoutItemProviderImpl].
 *
 * @param state The current [LazySwipeBannerState] containing information such as item count.
 * @param content The composable item content, receiving a scope and item index.
 * @param key Optional lambda to generate unique keys for each item by index.
 *
 * @return A lambda that returns the current [LazySwipeBannerLayoutItemProviderImpl].
 */
@Composable
internal fun rememberLazySwipeBannerItemProviderLambda(
    state: LazySwipeBannerState,
    content: LazySwipeBannerItemScope.()-> Unit,
    key: ((Int) -> Any)?
): () -> LazySwipeBannerLayoutItemProviderImpl {
    // Keeps the latest reference to content composable across recompositions
    val latestContent = rememberUpdatedState(content)
    val latestKey = rememberUpdatedState(key)
    return remember(
        state, latestContent, latestKey, state.itemCount
    ) {
        // Computes the interval content lazily when required
        val intervalContentState = derivedStateOf(referentialEqualityPolicy()) {
            SwipeBannerSwipeBannerLayoutIntervalContent(
                itemContent = latestContent.value,
                latestKey.value,
                state.itemCount
            )
        }

        // Lazily creates the item provider based on the interval content
        val itemProviderState = derivedStateOf(referentialEqualityPolicy()) {
            val intervalContent = intervalContentState.value
            LazySwipeBannerLayoutItemProviderImpl(
                intervalContent = intervalContent
            )
        }

        // Return the lambda that gives the latest item provider
        itemProviderState::value
    }
}
