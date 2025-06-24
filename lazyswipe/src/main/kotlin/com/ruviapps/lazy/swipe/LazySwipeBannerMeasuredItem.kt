package com.ruviapps.lazy.swipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed

/**
 * Represents one measured item of the lazy swipe banner
 */
internal class LazySwipeBannerMeasuredItem constructor(
    override val index: Int,
    private val placeables : List<Placeable>,
    override val isVertical: Boolean,
    private val horizontalAlignment: Alignment.Horizontal?,
    private val verticalAlignment: Alignment.Vertical?,
    private val layoutDirection: LayoutDirection,
    private val beforeContentPadding: Int,
    private val afterContentPadding: Int,
    override val key: Any,
    override val constraints: Constraints,
    override val contentType: Any?,
    /**
     * Extra spacing to be added to [size] aside from the sum of the [placeables] size. It is
     * usually representing the spacing after the item.
     */
    private val spacing: Int,
    private val visualOffset: IntOffset,
): LazySwipeBannerItemInfo, LazySwipeBannerLayoutMeasuredItem{
    override var offset: Int = 0
        private set
    override val size: Int
    override val mainAxisSizeWithSpacings: Int
    override val placeablesCount: Int
        get() = placeables.size
    override var nonScrollableItem: Boolean = false

    /** In the banner we have only single lane **/
    override val lane: Int = 0
    /** Each item takes one span **/
    override val span: Int = 1

    private var mainAxisLayoutSize: Int = Unset
    private var minMainAxisOffset: Int = 0
    private var maxMainAxisOffset: Int = 0

    // optimized for storing x and y offsets for each placeable one by one.
    // array's size == placeables.size * 2, first we store x, then y.
    private val placeableOffsets: IntArray
    /** Max of the cross axis sizes of all the inner placeables. */
    val crossAxisSize: Int

    init {
        var mainAxisSize = 0
        var maxCrossAxis = 0
        placeables.fastForEach {
            mainAxisSize += if(isVertical) it.height else it.width
            maxCrossAxis = maxOf(maxCrossAxis, if(!isVertical) it.height else it.width)
        }
        size = mainAxisSize
        mainAxisSizeWithSpacings = (size + spacing).coerceAtLeast(0)
        crossAxisSize = maxCrossAxis
        placeableOffsets = IntArray(placeables.size * 2)
    }

    /**
     * Update a [mainAxisLayoutSize] when the size did change after last [position] call
     */
    fun updateMainAxisLayoutSize(mainAxisLayoutSize: Int) {
        this.mainAxisLayoutSize = mainAxisLayoutSize
        maxMainAxisOffset = mainAxisLayoutSize + afterContentPadding
    }

    override fun getOffset(index: Int) =
        IntOffset(placeableOffsets[index * 2], placeableOffsets[index * 2 + 1])


    override fun position(
        mainAxisOffset: Int,
        crossAxisOffset: Int,
        layoutWidth: Int,
        layoutHeight: Int
    ) {
        position(mainAxisOffset,layoutWidth,layoutHeight)
    }
    fun position(mainAxisOffset: Int,
                 layoutWidth: Int,
                 layoutHeight: Int){
        this.offset = mainAxisOffset
        mainAxisLayoutSize = if (isVertical) layoutHeight else layoutWidth
        var mainAxisOffsetLocal = mainAxisOffset
        placeables.fastForEachIndexed { index, placeable ->
            val indexInArray = index * 2
            if(isVertical){
                if(horizontalAlignment != null){
                    placeableOffsets[indexInArray] = horizontalAlignment.align(placeable.width,layoutWidth,layoutDirection)
                    mainAxisOffsetLocal += placeable.height
                }
            }else{
                placeableOffsets[indexInArray] = mainAxisOffsetLocal
                if(verticalAlignment != null){
                    placeableOffsets[indexInArray + 1] = verticalAlignment.align(placeable.height,layoutHeight)
                    mainAxisOffsetLocal += placeable.width
                }
            }
        }
        minMainAxisOffset = -beforeContentPadding
        maxMainAxisOffset = mainAxisLayoutSize + afterContentPadding
    }

    override fun getParentData(index: Int) = placeables[index].parentData

    fun place(scope: Placeable.PlacementScope, isLookingAhead: Boolean,layer: GraphicsLayer? = null) =
        with(scope) {
            require(mainAxisLayoutSize != Unset) { "position() should be called first" }
            repeat(placeablesCount) { index ->
                val placeable = placeables[index]
                //val minOffset = minMainAxisOffset - placeable.mainAxisSize
                //val maxOffset = maxMainAxisOffset
                var offset = getOffset(index)
                offset += visualOffset
                if (isVertical) {
                    if (layer != null) {
                        placeable.placeWithLayer(offset, layer)
                    } else {
                        placeable.placeWithLayer(offset)
                    }
                } else {
                    if (layer != null) {
                        placeable.placeRelativeWithLayer(offset, layer)
                    } else {
                        placeable.placeRelativeWithLayer(offset)
                    }
                }
            }
        }


    private val IntOffset.mainAxis
        get() = if (isVertical) y else x

    private val Placeable.mainAxisSize
        get() = if (isVertical) height else width

    private inline fun IntOffset.copy(mainAxisMap: (Int) -> Int): IntOffset =
        if (isVertical) IntOffset(x, mainAxisMap(y)) else IntOffset(mainAxisMap(x), y)
}

private const val Unset = Int.MIN_VALUE