package com.ruviapps.lazy.swipe

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.GraphicsContext
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachReversed
import androidx.compose.ui.util.fastRoundToInt
import kotlinx.coroutines.CoroutineScope
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

internal fun measureLazySwipeBannerList(
    itemsCount: Int,
    measuredItemProvider: LazySwipeBannerMeasuredItemProvider,
    mainAxisAvailableSize: Int,
    beforeContentPadding: Int,
    afterContentPadding: Int,
    spaceBetweenItems: Int,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    scrollToBeConsumed: Float,
    constraints: Constraints,
    isVertical: Boolean,
    verticalArrangement: Arrangement.Vertical?,
    horizontalArrangement: Arrangement.Horizontal?,
    reverseLayout: Boolean,
    density: Density,
    beyondBoundsItemCount: Int,
    pinnedItems: List<Int>,
    hasLookaheadOccurred: Boolean,
    isLookingAhead: Boolean,
    approachLayoutInfo: LazySwipeBannerLayoutInfo?,
    coroutineScope: CoroutineScope,
    graphicsContext: GraphicsContext,
    layout: (Int, Int, Placeable.PlacementScope.() -> Unit) -> MeasureResult,
): LazySwipeBannerMeasureResult {
    require(beforeContentPadding >= 0 && afterContentPadding >= 0) {
        "invalid content padding before and after"
    }
    if (itemsCount <= 0) {
        //empty data set, reset the current scroll and report zero size
        var layoutWidth = constraints.minWidth
        val layoutHeight = constraints.minHeight
        return LazySwipeBannerMeasureResult(
            firstVisibleItem = null,
            firstVisibleItemScrollOffset = 0,
            canScrollForward = false,
            consumedScroll = 0f,
            measuredResult = layout(layoutWidth, layoutHeight) {},
            scrollBackAmount = 0f,
            visibleItemsInfo = emptyList(),
            viewportStartOffset = -beforeContentPadding,
            viewportEndOffset = mainAxisAvailableSize + afterContentPadding,
            totalItemsCount = 0,
            reverseLayout = reverseLayout,
            orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal,
            afterContentPadding = afterContentPadding,
            mainAxisItemSpacing = spaceBetweenItems,
            coroutineScope = coroutineScope,
            density = density,
            childConstraints = measuredItemProvider.childConstraints,
            remeasuredNeeded = false,
        )
    } else {
        var currentFirstItemIndex = firstVisibleItemIndex
        var currentFirstItemScrollOffset = firstVisibleItemScrollOffset

        if (currentFirstItemIndex >= itemsCount) {
            // the data set has been updated and now we have less items that we were
            // scrolled to before
            currentFirstItemIndex = itemsCount - 1
            currentFirstItemScrollOffset = 0
        }
        var scrollDelta = scrollToBeConsumed.fastRoundToInt()

        currentFirstItemScrollOffset -= scrollDelta

        if (currentFirstItemIndex == 0 && currentFirstItemScrollOffset < 0) {
            scrollDelta += currentFirstItemScrollOffset
            currentFirstItemScrollOffset = 0
        }
        val visibleItems = ArrayDeque<LazySwipeBannerMeasuredItem>()

        // include the start padding so we compose items in the padding area and neutralise item
        // spacing (if the spacing is negative this will make sure the previous item is composed)
        // before starting scrolling forward we will remove it back
        val minOffset = -beforeContentPadding + if (spaceBetweenItems < 0) spaceBetweenItems else 0
        val maxOffset = mainAxisAvailableSize

        currentFirstItemScrollOffset += minOffset
// max of cross axis sizes of all visible items
        var maxCrossAxis = 0

        // will be set to true if we composed some items only to know their size and apply scroll,
        // while in the end this item will not end up in the visible viewport. we will need an
        // extra remeasure in order to dispose such items.
        var remeasureNeeded = false

        // we had scrolled backward or we compose items in the start padding area, which means
        // items before current firstItemScrollOffset should be visible. compose them and update
        // firstItemScrollOffset
        while (currentFirstItemScrollOffset < 0 && currentFirstItemIndex > 0) {
            val previous = currentFirstItemIndex - 1
            val measuredItem = measuredItemProvider.getAndMeasure(previous)
            visibleItems.add(0, measuredItem)
            maxCrossAxis = maxOf(maxCrossAxis, measuredItem.crossAxisSize)
            currentFirstItemScrollOffset += measuredItem.mainAxisSizeWithSpacings
            currentFirstItemIndex = previous
        }

        // if we were scrolled backward, but there were not enough items before. this means
        // not the whole scroll was consumed
        if (currentFirstItemScrollOffset < minOffset) {
            val notConsumedScrollDelta = minOffset - currentFirstItemScrollOffset
            currentFirstItemScrollOffset = minOffset
            scrollDelta -= notConsumedScrollDelta
        }

        // neutralize previously added padding as we stopped filling the before content padding
        currentFirstItemScrollOffset -= minOffset

        var index = currentFirstItemIndex
        val maxMainAxis = (maxOffset + afterContentPadding).coerceAtLeast(0)
        var currentMainAxisOffset = -currentFirstItemScrollOffset

        // first we need to skip items we already composed while composing backward
        var indexInVisibleItems = 0
        while (indexInVisibleItems < visibleItems.size) {
            if (currentMainAxisOffset >= maxMainAxis) {
                // this item is out of the bounds and will not be visible.
                visibleItems.removeAt(indexInVisibleItems)
                remeasureNeeded = true
            } else {
                index++
                currentMainAxisOffset += visibleItems[indexInVisibleItems].mainAxisSizeWithSpacings
                indexInVisibleItems++
            }
        }

        // then composing visible items forward until we fill the whole viewport.
        // we want to have at least one item in visibleItems even if in fact all the items are
        // offscreen, this can happen if the content padding is larger than the available size.
        while (
            index < itemsCount &&
            (currentMainAxisOffset < maxMainAxis ||
                    currentMainAxisOffset <= 0 || // filling beforeContentPadding area
                    visibleItems.isEmpty())
        ) {
            val measuredItem = measuredItemProvider.getAndMeasure(index)
            currentMainAxisOffset += measuredItem.mainAxisSizeWithSpacings

            if (currentMainAxisOffset <= minOffset && index != itemsCount - 1) {
                // this item is offscreen and will not be visible. advance firstVisibleItemIndex
                currentFirstItemIndex = index + 1
                currentFirstItemScrollOffset -= measuredItem.mainAxisSizeWithSpacings
                remeasureNeeded = true
            } else {
                maxCrossAxis = maxOf(maxCrossAxis, measuredItem.crossAxisSize)
                visibleItems.add(measuredItem)
            }

            index++
        }

        val preScrollBackScrollDelta = scrollDelta
        // we didn't fill the whole viewport with items starting from firstVisibleItemIndex.
        // lets try to scroll back if we have enough items before firstVisibleItemIndex.
        if (currentMainAxisOffset < maxOffset) {
            val toScrollBack = maxOffset - currentMainAxisOffset
            currentFirstItemScrollOffset -= toScrollBack
            currentMainAxisOffset += toScrollBack
            while (
                currentFirstItemScrollOffset < beforeContentPadding && currentFirstItemIndex > 0
            ) {
                val previousIndex = currentFirstItemIndex - 1
                val measuredItem =
                    measuredItemProvider.getAndMeasure(previousIndex)
                visibleItems.add(0, measuredItem)
                maxCrossAxis = maxOf(maxCrossAxis, measuredItem.crossAxisSize)
                currentFirstItemScrollOffset += measuredItem.mainAxisSizeWithSpacings
                currentFirstItemIndex = previousIndex
            }
            scrollDelta += toScrollBack
            if (currentFirstItemScrollOffset < 0) {
                scrollDelta += currentFirstItemScrollOffset
                currentMainAxisOffset += currentFirstItemScrollOffset
                currentFirstItemScrollOffset = 0
            }
        }

        // report the amount of pixels we consumed. scrollDelta can be smaller than
        // scrollToBeConsumed if there were not enough items to fill the offered space or it
        // can be larger if items were resized, or if, for example, we were previously
        // displaying the item 15, but now we have only 10 items in total in the data set.
        val consumedScroll =
            if (
                scrollToBeConsumed.fastRoundToInt().sign == scrollDelta.sign &&
                abs(scrollToBeConsumed.fastRoundToInt()) >= abs(scrollDelta)
            ) {
                scrollDelta.toFloat()
            } else {
                scrollToBeConsumed
            }

        val unconsumedScroll = scrollToBeConsumed - consumedScroll
        // When scrolling to the bottom via gesture, there could be scroll back due to
        // not being able to consume the whole scroll. In that case, the amount of
        // scrollBack is the inverse of unconsumed scroll.
        val scrollBackAmount: Float =
            if (isLookingAhead && scrollDelta > preScrollBackScrollDelta && unconsumedScroll <= 0) {
                scrollDelta - preScrollBackScrollDelta + unconsumedScroll
            } else 0f

        // the initial offset for items from visibleItems list
        require(currentFirstItemScrollOffset >= 0) {
            "negative currentFirstItemScrollOffset"
        }
        val visibleItemsScrollOffset = -currentFirstItemScrollOffset
        var firstItem = visibleItems.first()

        // even if we compose items to fill before content padding we should ignore items fully
        // located there for the state's scroll position calculation (first item + first offset)
        if (beforeContentPadding > 0 || spaceBetweenItems < 0) {
            for (i in visibleItems.indices) {
                val size = visibleItems[i].mainAxisSizeWithSpacings
                if (
                    currentFirstItemScrollOffset != 0 &&
                    size <= currentFirstItemScrollOffset &&
                    i != visibleItems.lastIndex
                ) {
                    currentFirstItemScrollOffset -= size
                    firstItem = visibleItems[i + 1]
                } else {
                    break
                }
            }
        }

        // Compose extra items before
        val extraItemsBefore =
            createItemsBeforeList(
                currentFirstItemIndex = currentFirstItemIndex,
                measuredItemProvider = measuredItemProvider,
                beyondBoundsItemCount = beyondBoundsItemCount,
                pinnedItems = pinnedItems,
            )

        // Update maxCrossAxis with extra items
        extraItemsBefore.fastForEach { maxCrossAxis = maxOf(maxCrossAxis, it.crossAxisSize) }

        // Compose items after last item
        val extraItemsAfter =
            createItemsAfterList(
                visibleItems = visibleItems,
                measuredItemProvider = measuredItemProvider,
                itemsCount = itemsCount,
                beyondBoundsItemCount = beyondBoundsItemCount,
                pinnedItems = pinnedItems,
                consumedScroll = consumedScroll,
                isLookingAhead = isLookingAhead,
                lastApproachLayoutInfo = approachLayoutInfo,
            )

        // Update maxCrossAxis with extra items
        extraItemsAfter.fastForEach { maxCrossAxis = maxOf(maxCrossAxis, it.crossAxisSize) }

        val noExtraItems =
            firstItem == visibleItems.first() &&
                    extraItemsBefore.isEmpty() &&
                    extraItemsAfter.isEmpty()

        var layoutWidth =
            constraints.constrainWidth(if (isVertical) maxCrossAxis else currentMainAxisOffset)
        var layoutHeight =
            constraints.constrainHeight(if (isVertical) currentMainAxisOffset else maxCrossAxis)

        val positionedItems =
            calculateItemsOffsets(
                items = visibleItems,
                extraItemsBefore = extraItemsBefore,
                extraItemsAfter = extraItemsAfter,
                layoutWidth = layoutWidth,
                layoutHeight = layoutHeight,
                finalMainAxisOffset = currentMainAxisOffset,
                maxOffset = maxOffset,
                itemsScrollOffset = visibleItemsScrollOffset,
                isVertical = isVertical,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                reverseLayout = reverseLayout,
                density = density,
            )
        val firstVisibleIndex =
            if (noExtraItems) positionedItems.firstOrNull()
            else visibleItems.firstOrNull()?.index
        val lastVisibleIndex =
            if (noExtraItems) positionedItems.lastOrNull()
            else visibleItems.lastOrNull()?.index

        return LazySwipeBannerMeasureResult(
            firstVisibleItem = firstItem,
            firstVisibleItemScrollOffset = currentFirstItemScrollOffset,
            canScrollForward = index < itemsCount || currentMainAxisOffset > maxOffset,
            consumedScroll = consumedScroll,
            measuredResult =
                layout(layoutWidth, layoutHeight) {
                    // Tagging as motion frame of reference placement, meaning the placement
                    // contains scrolling. This allows the consumer of this placement offset to
                    // differentiate this offset vs. offsets from structural changes. Generally
                    // speaking, this signals a preference to directly apply changes rather than
                    // animating, to avoid a chasing effect to scrolling.
                    withMotionFrameOfReferencePlacement {
                        // place normal items
                        positionedItems.fastForEach { it.place(this, isLookingAhead) }

                    }

                    // we attach it during the placement so LazyListState can trigger re-placement
                    //placementScopeInvalidator.attachToScope()
                },
            scrollBackAmount = scrollBackAmount,
            visibleItemsInfo =
                updatedVisibleItems(
                    centeredVisibleItemIndex = firstVisibleItemIndex,
                    numberOfItemsPeekingEachSide = 3,
                    totalItems = itemsCount,
                    positionedItems = positionedItems,
                    ),
            viewportStartOffset = -beforeContentPadding,
            viewportEndOffset = maxOffset + afterContentPadding,
            totalItemsCount = itemsCount,
            reverseLayout = reverseLayout,
            orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal,
            afterContentPadding = afterContentPadding,
            mainAxisItemSpacing = spaceBetweenItems,
            remeasuredNeeded = remeasureNeeded,
            coroutineScope = coroutineScope,
            density = density,
            childConstraints = measuredItemProvider.childConstraints,
        )
    }
}

private fun calculateItemsOffsets(
    items: List<LazySwipeBannerMeasuredItem>,
    extraItemsBefore: List<LazySwipeBannerMeasuredItem>,
    extraItemsAfter: List<LazySwipeBannerMeasuredItem>,
    layoutWidth: Int,
    layoutHeight: Int,
    finalMainAxisOffset: Int,
    maxOffset: Int,
    itemsScrollOffset: Int,
    isVertical: Boolean,
    verticalArrangement: Arrangement.Vertical?,
    horizontalArrangement: Arrangement.Horizontal?,
    reverseLayout: Boolean,
    density: Density,
): MutableList<LazySwipeBannerMeasuredItem> {
    val mainAxisLayoutSize = if (isVertical) layoutHeight else layoutWidth
    val hasSpareSpace = finalMainAxisOffset < minOf(mainAxisLayoutSize, maxOffset)
    if (hasSpareSpace) {
        require(itemsScrollOffset == 0) { "non-zero itemsScrollOffset" }
    }

    val positionedItems =
        ArrayList<LazySwipeBannerMeasuredItem>(items.size + extraItemsBefore.size + extraItemsAfter.size)

    if (hasSpareSpace) {
        require(extraItemsBefore.isEmpty() && extraItemsAfter.isEmpty()) {
            "no extra items"
        }

        val itemsCount = items.size
        fun Int.reverseAware() = if (!reverseLayout) this else itemsCount - this - 1

        val sizes = IntArray(itemsCount) { index -> items[index.reverseAware()].size }
        val offsets = IntArray(itemsCount)
        if (isVertical) {
            require(verticalArrangement != null) {
                "null verticalArrangement when isVertical == true"
            }
            with(verticalArrangement){
                density.arrange(mainAxisLayoutSize, sizes, offsets)
            }
        } else {
            require(horizontalArrangement != null) {
                "null horizontalArrangement when isVertical == false"
            }
            with(horizontalArrangement) {
                // Enforces Ltr layout direction as it is mirrored with placeRelative later.
                density.arrange(mainAxisLayoutSize, sizes, LayoutDirection.Ltr, offsets)
            }
        }

        val reverseAwareOffsetIndices =
            if (!reverseLayout) offsets.indices else offsets.indices.reversed()
        for (index in reverseAwareOffsetIndices) {
            val absoluteOffset = offsets[index]
            // when reverseLayout == true, offsets are stored in the reversed order to items
            val item = items[index.reverseAware()]
            val relativeOffset =
                if (reverseLayout) {
                    // inverse offset to align with scroll direction for positioning
                    mainAxisLayoutSize - absoluteOffset - item.size
                } else {
                    absoluteOffset
                }
            item.position(relativeOffset,layoutWidth, layoutHeight)
            positionedItems.add(item)
        }
    } else {
        var currentMainAxis = itemsScrollOffset
        extraItemsBefore.fastForEach {
            currentMainAxis -= it.mainAxisSizeWithSpacings
            it.position(currentMainAxis,layoutWidth =layoutWidth, layoutHeight =layoutHeight)
            positionedItems.add(it)
        }

        currentMainAxis = itemsScrollOffset
        items.fastForEach {
            it.position(currentMainAxis,layoutWidth, layoutHeight)
            positionedItems.add(it)
            currentMainAxis += it.mainAxisSizeWithSpacings
        }

        extraItemsAfter.fastForEach {
            it.position(currentMainAxis, layoutWidth, layoutHeight)
            positionedItems.add(it)
            currentMainAxis += it.mainAxisSizeWithSpacings
        }
    }
    return positionedItems
}


private fun createItemsAfterList(
    visibleItems: MutableList<LazySwipeBannerMeasuredItem>,
    measuredItemProvider: LazySwipeBannerMeasuredItemProvider,
    itemsCount: Int,
    beyondBoundsItemCount: Int,
    pinnedItems: List<Int>,
    consumedScroll: Float,
    isLookingAhead: Boolean,
    lastApproachLayoutInfo: LazySwipeBannerLayoutInfo?,
): List<LazySwipeBannerMeasuredItem> {
    var list: MutableList<LazySwipeBannerMeasuredItem>? = null

    var end = visibleItems.last().index

    end = minOf(end + beyondBoundsItemCount, itemsCount - 1)

    for (i in visibleItems.last().index + 1..end) {
        if (list == null) list = mutableListOf()
        list.add(measuredItemProvider.getAndMeasure(i))
    }

    if (isLookingAhead) {
        // Check if there's any item that needs to be composed based on last approachLayoutInfo
        if (
            lastApproachLayoutInfo != null && lastApproachLayoutInfo.visibleItemsInfo.isNotEmpty()
        ) {
            // Find first item with index > end. Note that `visibleItemsInfo.last()` may not have
            // the largest index as the last few items could be added to animate item placement.
            val firstItem =
                lastApproachLayoutInfo.visibleItemsInfo.run {
                    var found: LazySwipeBannerItemInfo? = null
                    for (i in size - 1 downTo 0) {
                        if (this[i].index > end && (i == 0 || this[i - 1].index <= end)) {
                            found = this[i]
                            break
                        }
                    }
                    found
                }
            val lastVisibleItem = lastApproachLayoutInfo.visibleItemsInfo.last()
            if (firstItem != null) {
                for (i in firstItem.index..min(lastVisibleItem.index, itemsCount - 1)) {
                    // Only add to the list items that are _not_ already in the list.
                    if (list?.fastFirstOrNull { it.index == i } == null) {
                        if (list == null) list = mutableListOf()
                        list.add(measuredItemProvider.getAndMeasure(i))
                    }
                }
            }

            // Calculate the additional offset to subcompose based on what was shown in the
            // previous post-loookahead pass and the scroll consumed.
            val additionalOffset =
                lastApproachLayoutInfo.viewportEndOffset -
                        lastVisibleItem.offset -
                        lastVisibleItem.size -
                        consumedScroll
            if (additionalOffset > 0) {
                var index = lastVisibleItem.index + 1
                var totalOffset = 0
                while (index < itemsCount && totalOffset < additionalOffset) {
                    val item =
                        if (index <= end) {
                            visibleItems.fastFirstOrNull { it.index == index }
                        } else null ?: list?.fastFirstOrNull { it.index == index }
                    if (item != null) {
                        index++
                        totalOffset += item.mainAxisSizeWithSpacings
                    } else {
                        if (list == null) list = mutableListOf()
                        list.add(measuredItemProvider.getAndMeasure(index))
                        index++
                        totalOffset += list.last().mainAxisSizeWithSpacings
                    }
                }
            }
        }
    }

    // The list contains monotonically increasing indices.
    list?.let {
        if (it.last().index > end) {
            end = it.last().index
        }
    }
    pinnedItems.fastForEach { index ->
        if (index > end) {
            if (list == null) list = mutableListOf()
            list.add(measuredItemProvider.getAndMeasure(index))
        }
    }

    return list ?: emptyList()
}

private fun createItemsBeforeList(
    currentFirstItemIndex: Int,
    measuredItemProvider: LazySwipeBannerMeasuredItemProvider,
    beyondBoundsItemCount: Int,
    pinnedItems: List<Int>,
): List<LazySwipeBannerMeasuredItem> {
    var list: MutableList<LazySwipeBannerMeasuredItem>? = null

    var start = currentFirstItemIndex

    start = maxOf(0, start - beyondBoundsItemCount)

    for (i in currentFirstItemIndex - 1 downTo start) {
        if (list == null) list = mutableListOf()
        list.add(measuredItemProvider.getAndMeasure(i))
    }

    pinnedItems.fastForEachReversed { index ->
        if (index < start) {
            if (list == null) list = mutableListOf()
            list.add(measuredItemProvider.getAndMeasure(index))
        }
    }

    return list ?: emptyList()
}