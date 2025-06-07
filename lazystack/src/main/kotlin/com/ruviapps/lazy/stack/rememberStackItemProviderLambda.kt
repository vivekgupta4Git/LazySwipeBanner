package com.ruviapps.lazy.stack

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Remembers and returns a stable lambda that provides a [StackLazyLayoutItemProviderImpl],
 * backed by the current state, key generation logic, and content composable.
 *
 * This function ensures that recompositions of [LazyStackLayout] will efficiently reuse
 * the item provider unless relevant dependencies change.
 *
 * Internally, this constructs a single interval of items using [StackLayoutIntervalContent],
 * based on the current [LazyStackState.itemCount], and wraps it with a [StackLazyLayoutItemProviderImpl].
 *
 * @param state The current [LazyStackState] containing information such as item count.
 * @param content The composable item content, receiving a scope and item index.
 * @param key Optional lambda to generate unique keys for each item by index.
 *
 * @return A lambda that returns the current [StackLazyLayoutItemProviderImpl].
 */
@Composable
fun rememberStackItemProviderLambda(
    state: LazyStackState,
    content: LazyStackItemScope.()-> Unit,
    key: ((Int) -> Any)?
): () -> StackLazyLayoutItemProviderImpl {
    // Keeps the latest reference to content composable across recompositions
    val latestContent = rememberUpdatedState(content)
    val latestKey = rememberUpdatedState(key)
    return remember(
        state, latestContent, latestKey, state.itemCount
    ) {
        // Computes the interval content lazily when required
        val intervalContentState = derivedStateOf(referentialEqualityPolicy()) {
            StackLayoutIntervalContent(
                itemContent = latestContent.value,
                latestKey.value,
                state.itemCount
            )
        }

        // Lazily creates the item provider based on the interval content
        val itemProviderState = derivedStateOf(referentialEqualityPolicy()) {
            val intervalContent = intervalContentState.value
            StackLazyLayoutItemProviderImpl(
                intervalContent = intervalContent
            )
        }

        // Return the lambda that gives the latest item provider
        itemProviderState::value
    }
}
