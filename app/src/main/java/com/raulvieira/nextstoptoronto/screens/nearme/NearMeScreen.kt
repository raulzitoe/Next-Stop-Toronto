package com.raulvieira.nextstoptoronto.screens.nearme

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.*
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.components.InternetStatusBar
import com.raulvieira.nextstoptoronto.components.StopsPredictionLazyColumn
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.SinglePredictionModel
import com.raulvieira.nextstoptoronto.utils.locationFlow
import isInternetOn
import kotlinx.coroutines.delay

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun NearMeScreen(viewModel: NearMeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val isInternetOn by isInternetOn(LocalContext.current, scope).collectAsStateWithLifecycle()
    var internetStatusBarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isInternetOn) {
        internetStatusBarVisible = if (!isInternetOn) {
            true
        } else {
            delay(2000)
            false
        }
    }

    val permissionsState =
        rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(
            context
        )
    }

    LaunchedEffect(key1 = permissionsState.allPermissionsGranted, key2 = isInternetOn) {
        if (permissionsState.allPermissionsGranted && isInternetOn) {
            fusedLocationClient.locationFlow(this).collect { location ->
                if (viewModel.userLocation?.latitude != location?.latitude
                    && viewModel.userLocation?.longitude != location?.longitude
                ) {
                    location?.let {
                        viewModel.userLocationFlow.emit(it)
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionsState.launchMultiplePermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            viewModel.userLocation = null
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.near_me))
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!permissionsState.allPermissionsGranted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    PreciseLocationPermissionBox(permissionsState = permissionsState)
                }
            } else {
                Column {
                    AnimatedVisibility(
                        visible = internetStatusBarVisible,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        InternetStatusBar(isConnected = isInternetOn)
                    }
                    when (val state = uiState) {
                        is NearMeScreenState.Loading -> NearMeScreenLoading()
                        is NearMeScreenState.Success -> {
                            val stopPrediction = state.data.predictions
                            NearMeScreenSuccess(
                                predictions = stopPrediction,
                                onCalculateDistance = { stopTag ->
                                    viewModel.calculateStopDistance(
                                        stopTag
                                    )
                                },
                                onClickFavoriteItem = { isChecked, favoriteItem ->
                                    viewModel.handleFavoriteItem(
                                        isChecked,
                                        FavoritesModel(
                                            id = 0,
                                            routeTag = favoriteItem.routeTag,
                                            stopTag = favoriteItem.stopTag,
                                            stopTitle = favoriteItem.stopTitle
                                        )
                                    )
                                },
                                favoriteButtonChecked = { routeToCheck ->
                                    viewModel.isRouteFavorited(
                                        routeToCheck.stopTag,
                                        routeToCheck.routeTag,
                                        routeToCheck.stopTitle
                                    ).collectAsStateWithLifecycle(initialValue = false).value

                                }
                            )

                        }

                        is NearMeScreenState.Error -> NearMeScreenError()
                    }
                }
            }
        }
    }
}

@Composable
private fun NearMeScreenSuccess(
    predictions: List<RoutePredictionsModel>,
    onCalculateDistance: (String) -> String,
    onClickFavoriteItem: (Boolean, RoutePredictionsModel) -> Unit,
    favoriteButtonChecked: @Composable (RoutePredictionsModel) -> Boolean,
) {
    StopsPredictionLazyColumn(
        predictions = predictions.sortedBy { prediction ->
            onCalculateDistance(prediction.stopTag)
        },
        onClickFavoriteItem = { isChecked, favoriteItem ->
            onClickFavoriteItem(isChecked, favoriteItem)
        },
        favoriteButtonChecked = { routeToCheck ->
            favoriteButtonChecked(routeToCheck)
        },
        distanceToStop = { prediction ->
            onCalculateDistance(prediction.stopTag)
        }
    )
}

@Composable
private fun NearMeScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NearMeScreenError() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ERROR")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PreciseLocationPermissionBox(
    modifier: Modifier = Modifier,
    permissionsState: MultiplePermissionsState
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        //
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val allPermissionsRevoked =
                permissionsState.permissions.size ==
                        permissionsState.revokedPermissions.size

            val textToShow = if (!allPermissionsRevoked && permissionsState.shouldShowRationale) {
                // If not all the permissions are revoked, it's because the user accepted the COARSE
                // location permission, but not the FINE one.
                "App has access your approximate location but this feature requires precise location permission"
            } else if (permissionsState.shouldShowRationale) {
                // Both location permissions have been denied
                "This feature requires precise location permission."
            } else {
                // First time the user sees this feature or the user doesn't want to be asked again
                "This feature requires precise location permission. Open the configuration and change the location permission to precise"
            }

            val buttonText = if (!allPermissionsRevoked) {
                "Allow precise location"
            } else {
                "Request permissions"
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Warning, contentDescription = "Localized description")
                Text(text = textToShow)
            }
            if (!permissionsState.shouldShowRationale) {
                val context = LocalContext.current
                val uri = Uri.fromParts("package", context.packageName, null)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                Button(onClick = {
                    intent.data = uri
                    context.startActivity(intent)
                }) {
                    Text("Configuration")
                }
            } else {
                Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                    Text(buttonText)
                }
            }
        }
    }
}

@Preview
@Composable
private fun StopsLazyColumnPreview() {
    StopsPredictionLazyColumn(
        predictions = listOf(
            RoutePredictionsModel(
                routeTag = "41",
                stopTag = "1234",
                routeTitle = "41-Keele Towards somewhere",
                stopTitle = "Keele St at that St",
                directionTitleWhenNoPredictions = "41 - Keele some short turn",
                directions = listOf(
                    PredictionModel(
                        title = "41-Keele Towards somewhere",
                        predictions = listOf(
                            SinglePredictionModel(
                                "41",
                                vehicle = "1234",
                                minutes = "1",
                                seconds = "1"
                            ),
                            SinglePredictionModel(
                                "41",
                                vehicle = "1234",
                                minutes = "1",
                                seconds = "1"
                            )
                        )
                    )
                )
            ),
            RoutePredictionsModel(
                routeTag = "41",
                stopTag = "1234",
                routeTitle = "41-Keele Towards somewhere",
                stopTitle = "Keele St at that St",
                directionTitleWhenNoPredictions = "41 - Keele some short turn",
                directions = listOf(
                    PredictionModel(
                        title = "41-Keele Towards somewhere",
                        predictions = listOf(
                            SinglePredictionModel(
                                "41",
                                vehicle = "1234",
                                minutes = "1",
                                seconds = "1"
                            ),
                            SinglePredictionModel(
                                "41",
                                vehicle = "1234",
                                minutes = "1",
                                seconds = "1"
                            )
                        )
                    )
                )
            )
        ),
        onClickFavoriteItem = { _, _ -> },
        favoriteButtonChecked = { true },
        distanceToStop = { "0.2 Km" }
    )
}