package com.ruviapps.lazy.stack

/**
 * Represents a single interval in a stack-based lazy layout system.
 *
 * Each interval contains a block of items (often one or more composables) that can be lazily
 * displayed within a custom `LazyStack` layout. This class is a concrete implementation of
 * [StackLazyLayoutIntervalContent.Interval], and is used to associate:
 *
 * - A key generator lambda (for item identity and diffing)
 * - A content composable lambda that emits UI for a given index
 *
 * @property key A function that returns a stable key for a given local index within the interval.
 *               Keys help Compose track and reuse UI elements efficiently across recompositions.
 *               This can be `null`, in which case a default key will be used.
 *
 * @property item A composable lambda that emits the content for a given item index. This lambda is
 *                expected to accept a [LazyStackItemScope] and an integer index, and produce the
 *                corresponding UI content for that index.
 *
 * @see StackLazyLayoutIntervalContent
 * @see LazyStackItemScope
 * @see LazyStackLayoutComposable
 */
class StackLazyLayoutInterval(
    override val key: ((Int) -> Any)?,
    override val type: ((index: Int) -> Any?),
    val item: LazyStackLayoutComposable
) : StackLazyLayoutIntervalContent.Interval