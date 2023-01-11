package com.raulvieira.nextstoptoronto.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedSearchField(
    modifier: Modifier = Modifier,
    searchVisible: Boolean,
    searchedText: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    AnimatedVisibility(visible = searchVisible) {
        TextField(
            modifier = modifier,
            value = searchedText,
            onValueChange = { onValueChange(it) },
            label = { Text("Search") }
        )
    }
}