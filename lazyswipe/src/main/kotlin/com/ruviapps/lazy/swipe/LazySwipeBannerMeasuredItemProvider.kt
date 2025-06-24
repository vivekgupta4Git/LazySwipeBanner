package com.ruviapps.lazy.swipe

import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints

internal abstract class LazySwipeBannerMeasuredItemProvider(
    constraints: Constraints,
    isVertical : Boolean,
    private val itemProvider : LazySwipeBannerItemProvider,
    private val measureScope : LazySwipeBannerMeasureScope
) : LazySwipeBannerLayoutMeasuredItemProvider<LazySwipeBannerMeasuredItem>(){

    val childConstraints = Constraints(
        maxWidth = if(isVertical) constraints.maxWidth else Constraints.Infinity,
        maxHeight = if (!isVertical) constraints.maxHeight else Constraints.Infinity
    )

    fun getAndMeasure(
        index: Int,
        constraints: Constraints = childConstraints
    ): LazySwipeBannerMeasuredItem {
        val key = itemProvider.getKey(index)
        val contentType = itemProvider.getContentType(index)
        val placeables = measureScope.getPlaceable(index,constraints)
        return createItem(index,key,contentType,placeables,constraints)
    }


    fun keepAround(index: Int) {
        measureScope.compose(index)
    }

    abstract fun createItem(
        index : Int,
        key : Any,
        contentType : Any?,
        placeables : List<Placeable>,
        constraints: Constraints
    ) : LazySwipeBannerMeasuredItem
}