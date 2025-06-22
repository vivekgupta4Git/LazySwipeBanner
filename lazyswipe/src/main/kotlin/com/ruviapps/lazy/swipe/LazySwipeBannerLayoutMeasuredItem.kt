package com.ruviapps.lazy.swipe

import androidx.collection.mutableIntObjectMapOf
import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.fastForEach

internal interface LazySwipeBannerLayoutMeasuredItem{
    val index: Int
    val key: Any
    val isVertical: Boolean
    val mainAxisSizeWithSpacings: Int
    val placeablesCount: Int
    var nonScrollableItem: Boolean
    val constraints: Constraints
    val lane: Int
    val span: Int

    fun getOffset(index: Int): IntOffset

    fun position(mainAxisOffset: Int, crossAxisOffset: Int, layoutWidth: Int, layoutHeight: Int)

    fun getParentData(index: Int): Any?
}

internal abstract class LazySwipeBannerLayoutMeasuredItemProvider<T : LazySwipeBannerLayoutMeasuredItem> {
    /**
     * A cache of the previously composed items. It allows us to support [get] re-executions with
     * the same index during the same measure pass.
     */
    private val placeablesCache = mutableIntObjectMapOf<List<Placeable>>()

    abstract fun getAndMeasure(index: Int, lane: Int, span: Int, constraints: Constraints): T

    fun LazyLayoutMeasureScope.getPlaceable(
        index: Int,
        constraints: Constraints
    ) : List<Placeable>{
        val cachedPlaceable = placeablesCache[index]
        return  if(cachedPlaceable != null){
            cachedPlaceable
        }else{
            val measurables = compose(index)
            List(measurables.size){ i ->
                measurables[i].measure(constraints)
            }.also {
                placeablesCache[index] = it
            }
        }
    }
}

@Stable
sealed interface LazyLayoutMeasureScope : MeasureScope {

    /**
     * Compose an item of lazy layout.
     *
     * @param index the item index. Should be no larger that [LazySwipeBannerLayoutItemProviderImpl.itemCount].
     * @return List of [Measurable]s
     */
    fun compose( index: Int): List<Measurable>
}


internal fun <T : LazySwipeBannerLayoutMeasuredItem> updatedVisibleItems(
    centeredVisibleItemIndex: Int,
    numberOfItemsPeekingEachSide: Int,
    totalItems : Int,
    positionedItems: List<T>
): List<T> {
    if (positionedItems.isEmpty()) return emptyList()
    val finalVisibleItems = mutableListOf<T>()

    positionedItems.fastForEach { item ->
        if( item.index in centeredVisibleItemIndex .. centeredVisibleItemIndex + numberOfItemsPeekingEachSide){
            finalVisibleItems.add(item)
        }else if(
            item.index in (totalItems - numberOfItemsPeekingEachSide) until totalItems
        ){
            finalVisibleItems.add(item)
        }
    }
    return finalVisibleItems
}

private val LazySwipeBannerLayoutMeasuredItem.mainAxisOffset
    get() = getOffset(0).let { if (isVertical) it.y else it.x }

