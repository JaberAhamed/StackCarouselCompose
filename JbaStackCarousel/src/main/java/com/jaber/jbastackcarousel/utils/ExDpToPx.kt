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
package com.jaber.jbastackcarousel.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * To px
 *
 * @return the float value that indicate the pixel.
 */
@Composable
fun Dp.toPx(): Float = this.value * LocalDensity.current.density
