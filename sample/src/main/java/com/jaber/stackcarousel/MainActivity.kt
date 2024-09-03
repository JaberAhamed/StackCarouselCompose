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
package com.jaber.stackcarousel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jaber.jbastackcarousel.StackCarousel
import com.jaber.jbastackcarousel.StackType
import com.jaber.jbastackcarousel.rememberCarouselState
import com.jaber.stackcarousel.ui.theme.StackCarouselTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StackCarouselTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    StackCarouselSample(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun StackCarouselSample(modifier: Modifier = Modifier) {
    val images =
        listOf(
            R.drawable.stack_sample_one,
            R.drawable.stack_sample_two,
            R.drawable.stack_sample_three
        )

    val carouselState = rememberCarouselState(totalPageCount = images.size)

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(40.dp))

        StackCarousel(
            modifier =
            Modifier.padding(
                start = 30.dp,
                end = 30.dp
            ),
            state = carouselState,
            isEnableAnimation = true,
            stackType = StackType.Top,
            items = images
        ) { page: Int ->

            Image(
                painter = painterResource(id = page),
                modifier =
                Modifier
                    .height(400.dp)
                    .fillMaxWidth(),
                contentDescription = "",
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@PreviewLightDark
@Composable
private fun StackCarouselSamplePreview() {
    StackCarouselTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                StackCarouselSample()
            }
        }
    }
}
