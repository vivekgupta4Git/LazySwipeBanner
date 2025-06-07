package com.ruviapps.lazy.stack

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Remembers and returns a stable lambda that provides a [StackLazyLayoutItemProvider],
 * backed by the current state, key generation logic, and content composable.
 *
 * This function ensures that recompositions of [LazyStackLayout] will efficiently reuse
 * the item provider unless relevant dependencies change.
 *
 * Internally, this constructs a single interval of items using [StackLayoutIntervalContent],
 * based on the current [LazyStackState.itemCount], and wraps it with a [StackLazyLayoutItemProvider].
 *
 * @param state The current [LazyStackState] containing information such as item count.
 * @param pageContent The composable item content, receiving a scope and item index.
 * @param key Optional lambda to generate unique keys for each item by index.
 *
 * @return A lambda that returns the current [StackLazyLayoutItemProvider].
 */
@Composable
fun rememberStackItemProviderLambda(
    state: LazyStackState,
    pageContent: LazyStackLayoutComposable,
    key: ((Int) -> Any)?
): () -> StackLazyLayoutItemProvider {
    // Keeps the latest reference to content composable across recompositions
    val latestContent = rememberUpdatedState(pageContent)
    val latestKey = rememberUpdatedState(key)

    // Memoizes the item provider lambda unless any dependencies change
    return remember(
        state, latestContent, latestKey, state.itemCount
    ) {
        // Computes the interval content lazily when required
        val intervalContentState = derivedStateOf {
            StackLayoutIntervalContent(
                itemContent = latestContent.value,
                key = latestKey.value,
                size = state.itemCount
            )
        }

        // Lazily creates the item provider based on the interval content
        val itemProviderState = derivedStateOf {
            StackLazyLayoutItemProvider(
                intervalContent = intervalContentState.value
            )
        }

        // Return the lambda that gives the latest item provider
        itemProviderState::value
    }
}
