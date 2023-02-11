package com.raulvieira.nextstoptoronto.screens.map


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import com.raulvieira.nextstoptoronto.utils.locationFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.raulvieira.nextstoptoronto.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.ceil


const val START_ZOOM = 18.0
const val STARTING_LATITUDE = 43.656339
const val STARTING_LONGITUDE = -79.460403

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("ClickableViewAccessibility", "MissingPermission")
@Composable
fun MapView(
    modifier: Modifier = Modifier,
    onLoad: ((map: MapView) -> Unit)? = null,
    onRequestStopInfo: (stopId: String) -> Unit,
    stopState: StateFlow<StopPredictionModel?>,
    stopsList: List<StopModel>,
    onCloseStopInfo: () -> Unit
) {
    val mapViewState = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(
            context
        )
    }
    var userLocation: Location? = remember { null }
    val permissionsState =
        rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    var recalculateStops by remember { mutableStateOf(false) }

    if (permissionsState.allPermissionsGranted) {
        LaunchedEffect(key1 = fusedLocationClient.lastLocation) {
            if (permissionsState.allPermissionsGranted) {
                fusedLocationClient.locationFlow(this).collect { location ->
                    if (userLocation?.latitude != location?.latitude
                        && userLocation?.longitude != location?.longitude
                    ) {
                        userLocation = location
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = recalculateStops) {
        filterStopMarkersOverlay(
            context,
            stopsList,
            mapViewState,
            onRequestStopInfo = { stopId -> onRequestStopInfo(stopId) },
            stopState,
            coroutineScope,
            boundingBox = mapViewState.boundingBox
        )
    }

    DisposableEffect(key1 = lifecycle, key2 = stopsList) {
        val lifecycleObserver =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    recalculateStops = !recalculateStops
                }
            }

        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                with(mapViewState) {
                    // This fixes mapview overflowing parent layout
                    clipToOutline = true
                    controller.setCenter(GeoPoint(STARTING_LATITUDE, STARTING_LONGITUDE))
                    controller.setZoom(START_ZOOM)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                }
                val mapNorthCompassOverlay = object : CompassOverlay(context, mapViewState) {
                    override fun draw(c: Canvas?, pProjection: Projection?) {
                        drawCompass(c, -mapViewState.mapOrientation, pProjection?.screenRect)
                    }

                    override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                        if (e?.actionMasked == MotionEvent.ACTION_DOWN) {
                            if (mapView == null) return false
                            val mapViewPosition = IntArray(2)
                            mapView.getLocationOnScreen(mapViewPosition)
                            val frameLeft =
                                (ceil((35.0f * mScale - mCompassRoseBitmap.width / 2) + mapViewPosition[0]).toInt())
                            val frameTop =
                                (ceil((35.0f * mScale - mCompassRoseBitmap.height / 2 + mapViewPosition[1])).toInt())
                            val frameRight =
                                (ceil((35.0f * mScale + mCompassRoseBitmap.width / 2 + mapViewPosition[0])).toInt())
                            val frameBottom =
                                (ceil((35.0f * mScale + mCompassRoseBitmap.height / 2 + mapViewPosition[1])).toInt())

                            if (e.rawX > frameLeft && e.rawX < frameRight && e.rawY > frameTop && e.rawY < frameBottom) {
                                mapView.controller.animateTo(
                                    mapView.mapCenter, mapView.zoomLevelDouble,
                                    1000L, 0f
                                )
                                return true
                            }
                        }
                        return false
                    }
                }

                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapViewState).apply {
                    context.getDrawable(R.drawable.ic_navigation)?.let {
                        setDirectionIcon(drawableToBitmap(it))
                    }
                }

                // Override to improve map rotation smoothness - decreased deltaTime
                var timeLastSet = 0L
                val deltaTime = 10L
                var currentAngle = 0f
                val rotationOverlay = object : RotationGestureOverlay(mapViewState) {
                    override fun onRotate(deltaAngle: Float) {
                        currentAngle += deltaAngle
                        if (System.currentTimeMillis() - deltaTime > timeLastSet) {
                            timeLastSet = System.currentTimeMillis()
                            mapViewState.mapOrientation = mapViewState.mapOrientation + currentAngle
                        }
                    }
                }

                val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        onCloseStopInfo()
                        InfoWindow.closeAllInfoWindowsOn(mapViewState)
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        return false
                    }
                })

                val overlays =
                    listOf(
                        rotationOverlay,
                        RadiusMarkerClusterer(context),
                        locationOverlay,
                        mapNorthCompassOverlay
                    )
                mapViewState.overlays.addAll(overlays)
                // Events Overlays needs to be first to listen to events
                mapViewState.overlays.add(0, mapEventsOverlay)
                mapViewState
            },
            modifier = modifier
        ) { mapView ->
            mapViewState.setOnTouchListener { view, motionEvent ->
                if (motionEvent.actionMasked == MotionEvent.ACTION_UP) {
                    recalculateStops = !recalculateStops
                    view.invalidate()
                }


                view.performClick()
            }
            onLoad?.invoke(mapView)
        }

        IconButton(modifier = Modifier
            .padding(20.dp)
            .align(Alignment.BottomEnd), onClick = {
            val geoPoint = userLocation?.let { GeoPoint(it) }
            geoPoint?.let {
                mapViewState.isAnimating
                mapViewState.controller.animateTo(
                    it, mapViewState.zoomLevelDouble,
                    1000L, mapViewState.mapOrientation
                )
                mapViewState.findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.IO) {
                    while (mapViewState.isAnimating) {
                        delay(50)
                    }
                    recalculateStops = !recalculateStops
                }

            }
        }) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.background)) {
                Icon(
                    modifier = Modifier.size(20.dp).align(Alignment.Center),
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Center my location",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

        }
    }
}

private fun isStopWithinBoundBox(
    stopLatitude: Double,
    stopLongitude: Double,
    boundingBox: BoundingBox
): Boolean {
    if (stopLatitude > boundingBox.latSouth && stopLatitude < boundingBox.latNorth
        && stopLongitude < boundingBox.lonEast && stopLongitude > boundingBox.lonWest
    ) {
        return true
    }
    return false
}

private fun filterStopMarkersOverlay(
    context: Context,
    stopsList: List<StopModel>,
    mapView: MapView,
    onRequestStopInfo: (stopId: String) -> Unit,
    stopState: StateFlow<StopPredictionModel?>,
    coroutineScope: CoroutineScope,
    boundingBox: BoundingBox
) {
    val stopMarkersOverlay = RadiusMarkerClusterer(context)
    for (stop in stopsList) {
        if (!isStopWithinBoundBox(
                stop.latitude.toDouble(),
                stop.longitude.toDouble(),
                boundingBox = boundingBox
            )
        ) continue

        val marker = Marker(mapView)

        marker.position = GeoPoint(stop.latitude.toDouble(), stop.longitude.toDouble())
        marker.icon = context.getDrawable(R.drawable.ic_bus_stop)
        marker.setOnMarkerClickListener { thisMarker, _ ->
            onRequestStopInfo(stop.stopId)
            coroutineScope.launch {
                stopState.collect { stopPrediction ->
                    if (stopPrediction != null) {
                        var textString: String = stop.title
                        thisMarker.showInfoWindow()
                        if (stopPrediction.predictions.isNotEmpty()) {
                            stopPrediction.predictions.forEach { route ->
                                if (route.directions.isNotEmpty()) {
                                    val routeDirection =
                                        route.directions[0].title.substringBefore(" ")
                                    textString += "\n" + route.routeTag + " - " + routeDirection + " in: " + route.directions[0].predictions.first().minutes + " min"
                                }
                            }
                        } else {
                            textString += "\n Loading..."
                        }
                        thisMarker.title = textString
                        thisMarker.showInfoWindow()
                    }
                }
            }
            true
        }

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        stopMarkersOverlay.add(marker)
    }
    mapView.overlays[2] = stopMarkersOverlay
    mapView.invalidate()
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}