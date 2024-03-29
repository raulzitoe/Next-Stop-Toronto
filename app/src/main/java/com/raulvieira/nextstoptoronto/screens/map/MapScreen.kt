package com.raulvieira.nextstoptoronto.screens.map

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.raulvieira.nextstoptoronto.components.InternetStatusBar
import isInternetOn
import kotlinx.coroutines.delay


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapScreenViewModel = hiltViewModel()
) {
    val permissionsState =
        rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val isInternetOn by isInternetOn(LocalContext.current, scope).collectAsStateWithLifecycle()
    var internetStatusBarVisible by remember { mutableStateOf(false) }
    val stopsList by viewModel.stopList.collectAsStateWithLifecycle()

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionsState.permissions.first().launchPermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isInternetOn) {
        internetStatusBarVisible = if (!isInternetOn) {
            true
        } else {
            delay(2000)
            false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AnimatedVisibility(
                visible = internetStatusBarVisible,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                InternetStatusBar(isConnected = isInternetOn)
            }
            MapView(
                modifier = Modifier.fillMaxSize(),
                onRequestStopInfo = { stopId -> viewModel.setStopIdValue(stopId) },
                stopState = viewModel.stopState,
                stopsList = stopsList,
                onCloseStopInfo = { viewModel.clearStopIdFlow() }
            )
        }

    }
}