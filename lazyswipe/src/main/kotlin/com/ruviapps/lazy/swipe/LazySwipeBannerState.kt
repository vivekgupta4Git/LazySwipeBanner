package com.ruviapps.lazy.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize

/**
 * Represents the state of a LazySwipeBanner, which is a custom layout for swiping through items.
 *
 * @property itemCount The total number of items in the banner. Must be at least 3.
 * @property initialIndex The initial index to start the banner from.
 * @property orientation The orientation of the swipe gesture, either horizontal or vertical.
 *
 * @constructor Initializes a new instance of LazySwipeBannerState with the given parameters.
 * @throws IllegalArgumentException if itemCount is less than 3.
 */
class LazySwipeBannerState(
    val itemCount: Int = 3,
    initialIndex: Int = 0,
    val orientation: Orientation = Orientation.Horizontal
) {
    init {
        require(itemCount >= 3) {
            error("Lazy Stack requires at least 3 items.")
        }
    }

    /**
     * The index of the item which is currently centered in the banner.
     */
    var currentIndex by mutableIntStateOf(initialIndex)
        private set

    /**
     * This is what make swipe works. Its value is used in the modifier [LazySwipeBannerItemAnimationConfig] to
     * translate Center item along axis depending upon the orientation of the [LazySwipeBanner]
     */
    internal val swipeOffset by mutableStateOf(Animatable(0f))

    /**
     * Decreases the current index by one, wrapping around to the start of the list if at the end.
     * This is called when the user swipes to the left (if horizontal) down (if vertical).
     * The [IntSize] parameter is used for animation
     *
     * @param size The size of the layout [LazySwipeBanner], in pixels.
     */
    suspend fun decreaseIndex(size: IntSize) {
        val (dimension, index) = if (orientation == Orientation.Vertical)
            Pair(size.height.toFloat(), currentIndex - 1)
        else
            Pair(size.width.toFloat(), currentIndex - 1)

        currentIndex = wrapIndex(index)
        //snap to opposite direction so that new card reveal looks more natural
        swipeOffset.snapTo(-dimension)
        //once it snapped to opposite direction bring it to the center again.
        swipeOffset.animateTo(0f)
    }


    /**
     * Increases the current index by one, wrapping around to the start of the list if at the end.
     * This is called when the user swipes to the right (if horizontal) or up (if vertical).
     * The [IntSize] parameter is used for animation.
     *
     * @param size The size of the layout [LazySwipeBanner], in pixels.
     */
    suspend fun increaseIndex(size: IntSize) {
        val (dimension, index) = if (orientation == Orientation.Vertical)
            Pair(size.height.toFloat(), currentIndex + 1)
        else
            Pair(size.width.toFloat(), currentIndex + 1)  //  swipeOffset.animateTo(-dimension)
        currentIndex = wrapIndex(index)
        //snap to opposite direction so that new card reveal looks more natural
        swipeOffset.snapTo(dimension)
        //once it snapped to opposite direction bring it to the center again.
        swipeOffset.animateTo(0f)
    }

    /**
     * Wraps the given index around the list of items, so that if [index] is out of range,
     * it is mapped to a valid index within the list.
     *
     * @param index The index to wrap.
     * @return The wrapped index.
     */
    private fun wrapIndex(index: Int): Int {
        return (index + itemCount) % itemCount
    }

    /**
     * Instantly moves the current index to the provided index, and resets the swipe offset
     * to the center of the layout.
     *
     * @param index The new index to snap to.
     */
    suspend fun snapTo(index: Int) {
        if (currentIndex == index) return
        currentIndex = wrapIndex(index)
        swipeOffset.snapTo(0f)
    }

    /**
     * Animates the current index to the provided index, animating the swipe offset
     * as if the user had swiped to the new index.
     *
     * @param index The new index to animate to.
     * @param animateByDistance The distance to animate the swipe offset by.
     * @param animationSpec The animation spec for the animation.
     */
    suspend fun animateTo(
        index: Int,
        animationSpec: AnimationSpec<Float> =
            spring(
                stiffness = Spring.StiffnessVeryLow,
                dampingRatio = Spring.DampingRatioMediumBouncy
            ),
        animateByDistance: Float = 1500f
    ) {
        if (currentIndex == index) return
        currentIndex = wrapIndex(index)
        swipeOffset.animateTo(animateByDistance, animationSpec)
        swipeOffset.snapTo(-animateByDistance)
        swipeOffset.animateTo(0f, animationSpec)
    }

    companion object {
        fun Saver(itemCount: Int): Saver<LazySwipeBannerState, *> =
            Saver(
                save = {
                    listOf(it.currentIndex, it.orientation.ordinal)
                },
                restore = { list ->
                    val saveIndex = list[0]
                    val orientation = Orientation.entries.toTypedArray().getOrElse(list[1]) {
                        Orientation.Horizontal
                    }
                    LazySwipeBannerState(
                        itemCount = itemCount,
                        initialIndex = saveIndex,
                        orientation = orientation
                    )

                }
            )
    }
}

/**
 * Remembers and returns a [LazySwipeBannerState] with the specified parameters.
 *
 * This function uses [rememberSaveable] to retain the state across configuration changes
 * and process death, ensuring that the banner's state is preserved.
 *
 * @param itemCount The total number of items in the banner. Defaults to 3.
 * @param initialIndex The initial index to start the banner from. Defaults to 0.
 * @param orientation The orientation of the swipe gesture, either horizontal or vertical.
 *                    Defaults to [Orientation.Horizontal].
 * @return A [LazySwipeBannerState] initialized with the provided parameters.
 */
@Composable
fun rememberLazyStackState(
    itemCount: Int = 3,
    initialIndex: Int = 0,
    orientation: Orientation = Orientation.Horizontal
): LazySwipeBannerState {
    return rememberSaveable(saver = LazySwipeBannerState.Saver(itemCount), key = "$orientation") {
        LazySwipeBannerState(itemCount, initialIndex, orientation)
    }
}

