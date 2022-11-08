package com.raulvieira.nextstoptoronto.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raulvieira.nextstoptoronto.navigation.Screen

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(text = "Home Screen", modifier = Modifier.clickable { onNavigate(Screen.SecondScreen) })
    }
}