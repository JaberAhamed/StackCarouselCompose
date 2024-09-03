/*
 * Copyright 2024 Jaber Bin Ahamed. All rights reserved.
 *
 * ------------------------------------------------------------------------
 *
 * Project: StackCarousel
 * Developed by: Jaber Bin Ahamed
 *
 * Source:
 */
package com.jaber.jbastackcarousel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.jaber.jbastackcarousel.utils.toPx
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

/**
 * A composable function that creates a stack-based carousel, useful for implementing swipe able card stacks or similar UI patterns in Jetpack Compose.
 *
 * @param T The type of the items to be displayed in the carousel.
 * @param state The state of the carousel, which manages the current position and provides control over the carousel's behavior.
 * @param items A list of items to be displayed in the carousel. Each item will be passed to the [content] composable for rendering.
 * @param modifier An optional [Modifier] to apply to the carousel layout. Defaults to [Modifier] if not provided.
 * @param swipeType The type of swipe gesture to support. Defaults to [SwipeType.Default]. Customize this to control the swipe behavior.
 * @param stackType The stacking order of the items in the carousel. Defaults to [StackType.Top], which stacks items from the top. Change this to adjust the stack's appearance.
 * @param isEnableAnimation A Boolean flag to enable or disable animations during item transitions. Defaults to true, meaning animations are enabled by default.
 * @param content A composable lambda that defines how each item should be displayed. It receives the current item of type [T] as a parameter.
 */

@Composable
fun <T> StackCarousel(
    state: CarouselState,
    items: List<T>,
    modifier: Modifier = Modifier,
    swipeType: SwipeType = SwipeType.Default,
    stackType: StackType = StackType.Bottom,
    isEnableAnimation: Boolean = true,
    content: @Composable (page: T) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val images =
        remember {
            items.toMutableStateList()
        }
    var currentIndex by remember { mutableIntStateOf(0) }
    var currentIndicator by remember { mutableIntStateOf(0) }
    val imageCount = state.totalPage
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val animScaleX =
        remember {
            Animatable(0.9f)
                .apply { updateBounds(0.9f, 1f) }
        }
    var draggableTranslateYValue by remember { mutableFloatStateOf(8f) }
    val translationYa by animateFloatAsState(
        targetValue =
        if (isEnableAnimation) {
            if (offsetX.value.absoluteValue in 0.0..100.0) {
                draggableTranslateYValue = if (stackType == StackType.Top) -8f else 8f
                draggableTranslateYValue
            } else {
                if (draggableTranslateYValue >= 0 && stackType == StackType.Bottom) {
                    draggableTranslateYValue -= 0.5f
                } else if (draggableTranslateYValue <= 0) {
                    draggableTranslateYValue += 0.5f
                }
                draggableTranslateYValue
            }
        } else {
            if (stackType == StackType.Top) -8f else 8f
        },
        label = "",
        animationSpec =
        if (offsetX.value.absoluteValue in 0.0..100.0) {
            tween(300)
        } else {
            spring()
        }
    )
    var alphaValue by remember { mutableFloatStateOf(0.8f) }
    val afterNextPadding = if (stackType == StackType.Top) -6.dp.toPx() else 6.dp.toPx()
    val nextAlpha: Float by animateFloatAsState(
        if (offsetX.value.absoluteValue in 0.0..100.0) {
            if (alphaValue >= 0.8f) {
                alphaValue -= 0.01f
            }
            alphaValue
        } else {
            if (alphaValue <= 1) {
                alphaValue += 0.01f
            }
            alphaValue
        },
        label = ""
    )

    Box(
        modifier =
        modifier.pointerInput(Unit) {
            if (items.size >= 2) {
                detectHorizontalDragGestures(onDragEnd = {
                    coroutineScope.launch {
                        if (offsetX.value > 100 || offsetX.value < -100) {
                            currentIndicator = (currentIndicator + 1) % imageCount
                            offsetX.animateTo(
                                if (offsetX.value < 0) {
                                    screenWidth.toPx().unaryMinus()
                                } else {
                                    screenWidth.toPx()
                                },
                                tween(150)
                            )
                            currentIndex = 0
                            swapFirstToLast(images)
                            alphaValue = 0.8f
                            offsetX.snapTo(0f)
                        } else {
                            offsetX.snapTo(0f)
                        }
                    }
                }) { _, dragAmount ->

                    coroutineScope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount)
                    }
                }
            }
        }
    ) {
        LaunchedEffect(currentIndicator) {
            state.currentPage = currentIndicator
        }

        if (isEnableAnimation) {
            LaunchedEffect(offsetX.value) {
                if (offsetX.value.absoluteValue in 0.0..100.0) {
                    animScaleX.animateTo(0.9f)
                } else {
                    animScaleX.animateTo(animScaleX.value + 0.01f)
                }
            }
        }

        for (index in images.size - 1 downTo 0) {
            val isCurrent = index == currentIndex
            val translationX = if (isCurrent) offsetX.value else 0f
            val isNext = (index == (currentIndex + 1) % imageCount)
            val translationY =
                if (isCurrent) {
                    0.dp.toPx()
                } else if (isNext) {
                    if (stackType == StackType.Top) -8f.dp.toPx() else 8f.dp.toPx()
                } else {
                    if (stackType == StackType.Top) -14f.dp.toPx() else 14f.dp.toPx()
                }

            // Use for visibility of the stack carousel card
            val alpha =
                if (isCurrent) {
                    1f
                } else if (isNext) {
                    0.8f
                } else {
                    0.5f
                }

            // X-axis scale
            val scaleX =
                if (isCurrent) {
                    1f - (offsetX.value.absoluteValue / 1000)
                } else {
                    1f
                }

            // Y-axis scale
            val scaleY =
                if (isCurrent) {
                    1f - (offsetX.value.absoluteValue / 1000)
                } else {
                    1f
                }

            if (isCurrent) {
                Box(
                    modifier =
                    Modifier
                        .graphicsLayer(
                            swipeType = swipeType,
                            alpha = alpha,
                            translationX = translationX,
                            translationY = translationY,
                            offsetX = offsetX.value,
                            scaleX = scaleX,
                            scaleY = scaleY
                        )
                        .shadow(
                            elevation = 2.8.dp,
                            shape = RoundedCornerShape(size = 12.dp),
                            ambientColor = Color.Black.copy(0.8f),
                            spotColor = Color.Black.copy(0.3f)
                        )
                ) {
                    content(images[index])
                }
            } else if (isNext) {
                Box(
                    modifier =
                    Modifier
                        .scale(scaleX = animScaleX.value, 1f)
                        .graphicsLayer(
                            clip = true,
                            shape = RoundedCornerShape(12.dp),
                            translationX = translationX,
                            alpha = nextAlpha,
                            translationY = if (isEnableAnimation) translationYa.dp.toPx() else translationY,
                            shadowElevation = 20.5f
                        )
                        .shadow(
                            elevation = 2.8.dp,
                            shape = RoundedCornerShape(size = 12.dp),
                            ambientColor = Color.Black.copy(0.8f),
                            spotColor = Color.Black.copy(0.3f)
                        )
                ) {
                    content(images[index])
                }
            } else {
                Box(
                    modifier =
                    Modifier
                        .scale(scaleX = 0.8f, 1f)
                        .graphicsLayer(
                            clip = true,
                            shape = RoundedCornerShape(12.dp),
                            translationX = translationX,
                            alpha = alpha,
                            translationY = if (offsetX.value == 0f) translationYa.dp.toPx() + afterNextPadding else translationY,
                            shadowElevation = 20.5f
                        )
                        .shadow(
                            elevation = 2.8.dp,
                            shape = RoundedCornerShape(size = 12.dp),
                            ambientColor = Color.Black.copy(0.8f),
                            spotColor = Color.Black.copy(0.3f)
                        )
                ) {
                    content(images[index])
                }
            }
        }
    }
}

/**
 * An enumeration that defines the types of swipe gestures supported by the [StackCarousel].
 *
 * The [SwipeType] enum provides different visual effects that can be applied when the user swipes through items
 * in the carousel.
 */
enum class SwipeType {
    /**
     * The default swipe behavior with no additional transformations.
     * This provides a simple swipe with no extra visual effects.
     */
    Default,

    /**
     * Applies a rotation effect to the items as they are swiped.
     * This creates a rotating effect as the user swipes through the carousel.
     */
    Rotate,

    /**
     * Applies a scaling effect to the items as they are swiped.
     * This effect scales the items, making them appear to small as they move.
     */
    Scale
}

/**
 * An enumeration that defines the stacking order of items in the [StackCarousel].
 *
 * The [StackType] enum allows you to control whether items in the carousel are stacked from the bottom or the top.
 */
enum class StackType {
    /**
     * Stacks the items from the bottom, meaning the first item is at the top,
     * and subsequent items are layered on bottom.
     */
    Bottom,

    /**
     * Stacks the items from the top, meaning the first item is at the bottom,
     * and subsequent items are layered on top.
     */
    Top
}

@Stable
private fun Modifier.graphicsLayer(
    swipeType: SwipeType,
    alpha: Float,
    translationX: Float,
    translationY: Float,
    offsetX: Float,
    scaleX: Float,
    scaleY: Float
): Modifier {
    if (swipeType == SwipeType.Default) {
        return this.graphicsLayer(
            clip = true,
            shape = RoundedCornerShape(12.dp),
            translationX = translationX,
            alpha = alpha,
            shadowElevation = 20.5f,
            translationY = translationY
        )
    }

    if (swipeType == SwipeType.Rotate) {
        return this.graphicsLayer(
            translationX = translationX,
            alpha = alpha,
            translationY = translationY,
            ambientShadowColor = Color.White,
            scaleX = scaleX,
            scaleY = scaleY,
            rotationZ = offsetX
        )
    }

    if (swipeType == SwipeType.Scale) {
        return this.graphicsLayer(
            translationX = translationX,
            alpha = alpha,
            translationY = translationY,
            ambientShadowColor = Color.White,
            scaleX = scaleX,
            scaleY = scaleY
        )
    }

    return this.graphicsLayer(
        clip = true,
        shape = RoundedCornerShape(12.dp),
        translationX = translationX,
        alpha = alpha,
        shadowElevation = 20.5f,
        translationY = translationY
    )
}

/**
 * A state class that holds and manages the state for the [StackCarousel] composable.
 *
 * The [CarouselState] class is responsible for tracking the total number of pages and the current page
 * being displayed in the carousel. This state can be used to control the behavior of the carousel
 * and to respond to user interactions.
 *
 * @param totalPage The total number of pages in the carousel.
 * @param initialPage The initial page to display when the carousel is first shown. Defaults to 0.
 *
 * @property currentPage The currently displayed page in the carousel. This can be modified to programmatically
 * change the current page.
 */
@Stable
class CarouselState(
    val totalPage: Int,
    initialPage: Int = 0
) {
    /**
     * The currently displayed page in the carousel.
     *
     * This value is mutable and can be observed to react to page changes. Modifying this value
     * will update the displayed page in the [StackCarousel].
     */
    var currentPage by mutableIntStateOf(initialPage)
}

/**
 * Swaps the first element of a mutable list with the last element, shifting all other elements one position to the left.
 *
 * This function is useful for scenarios where you need to move the first item of a list to the end while preserving
 * the order of the other elements. If the list contains fewer than two elements, the function will return without making any changes.
 *
 * @param T The type of elements in the list.
 * @param items A mutable list of items. The first item in the list will be moved to the last position,
 * and all other items will be shifted one position to the left.
 *
 * @throws IndexOutOfBoundsException if the list is empty or has fewer than two elements.
 */
fun <T> swapFirstToLast(items: MutableList<T>) {
    if (items.size < 2) {
        return
    }

    val firstElement = items[0]

    for (i in 0 until items.size - 1) {
        items[i] = items[i + 1]
    }

    items[items.size - 1] = firstElement
}

@Composable
fun rememberCarouselState(totalPageCount: Int): CarouselState = remember { CarouselState(totalPage = totalPageCount) }
