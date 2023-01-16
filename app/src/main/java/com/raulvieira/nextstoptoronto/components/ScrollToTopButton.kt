package com.raulvieira.nextstoptoronto.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScrollToTopButton(onClick: () -> Unit, showButton: Boolean) {
    AnimatedVisibility(
        visible = showButton,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp), Alignment.BottomCenter
        ) {
            Button(
                onClick = { onClick() }, modifier = Modifier
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, "arrow up")
            }
        }
    }

}