package com.raulvieira.nextstoptoronto.screens.map

import android.Manifest
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapScreenViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit = {},
    routeTag: String = "",
    stopTagToCenter: String = ""
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
    val paths by viewModel.paths.collectAsStateWithLifecycle()
    val locationToCenter by remember(stopsList) {
        mutableStateOf(
            stopsList.find { it.stopTag == stopTagToCenter }?.let {
                Location("")
                    .apply {
                        latitude = it.latitude.toDoubleOrNull() ?: 0.0
                        longitude = it.longitude.toDoubleOrNull() ?: 0.0
                    }
            }
        )
    }

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

    Scaffold(
        topBar = {
            if (routeTag.isNotBlank()) {
                TopAppBar(
                    title = { Text(text = "Route: $routeTag") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back button")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
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
                paths = paths,
                locationToCenter = locationToCenter,
                onCloseStopInfo = { viewModel.clearStopIdFlow() }
            )
        }
    }
}