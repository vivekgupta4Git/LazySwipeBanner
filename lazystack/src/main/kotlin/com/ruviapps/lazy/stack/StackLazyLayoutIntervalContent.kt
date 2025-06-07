package com.ruviapps.lazy.stack

import androidx.compose.foundation.lazy.layout.IntervalList
import androidx.compose.foundation.lazy.layout.getDefaultLazyLayoutKey

abstract class StackLazyLayoutIntervalContent<Interval : StackLazyLayoutIntervalContent.Interval> {
    abstract val intervals: IntervalList<Interval>

    /** The total amount of items in all the intervals. */
    val itemCount: Int
        get() = intervals.size

    /** Returns item key based on a global index. */
    fun getKey(index: Int): Any =
        withInterval(index) { localIndex, content ->
            content.key?.invoke(localIndex) ?: getDefaultLazyLayoutKey(index)
        }

    /** Returns content type based on a global index. */
    fun getContentType(index: Int): Any? =
        withInterval(index) { localIndex, content -> content.type.invoke(localIndex) }

    /**
     * Runs a [block] on the content of the interval associated with the provided [globalIndex] with
     * providing a local index in the given interval.
     */
    inline fun <T> withInterval(
        globalIndex: Int,
        block: (localIntervalIndex: Int, content: Interval) -> T
    ): T {
        val interval = intervals[globalIndex]
        val localIntervalIndex = globalIndex - interval.startIndex
        return block(localIntervalIndex, interval.value)
    }

    /**
     * Common content of individual intervals in `item` DSL of lazy layouts.
     */
    interface Interval {
        /** Returns item key based on a local index for the current interval. */
        val key: ((index: Int) -> Any)?
            get() = null

        /** Returns item type based on a local index for the current interval. */
        val type: ((index: Int) -> Any?)
            get() = { null }
    }
}