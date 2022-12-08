package com.raulvieira.nextstoptoronto.screens.map


import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


const val START_ZOOM = 18.0
const val STARTING_LATITUDE = 43.656339
const val STARTING_LONGITUDE = -79.460403

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    onLoad: ((map: MapView) -> Unit)? = null,
    onRequestStopInfo: (stopId: String) -> Unit,
    stopState: StateFlow<StopPredictionModel>,
    stopsList: List<StopModel>
) {
    val mapViewState = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AndroidView(
        factory = { mapViewState },
        modifier = modifier
    ) { mapView ->

        with(mapView) {
            // Clear overlays to avoid duplicating
            overlays.clear()
            // This fixes mapview overflowing parent layout
            clipToOutline = true

            controller.setZoom(START_ZOOM)
            controller.setCenter(GeoPoint(STARTING_LATITUDE, STARTING_LONGITUDE))
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        }

        val compassOverlay =
            CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
        compassOverlay.enableCompass()
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)

        // Override to improve map rotation smoothness - decreased deltaTime
        var timeLastSet = 0L
        val deltaTime = 10L
        var currentAngle = 0f
        val rotationOverlay = object : RotationGestureOverlay(mapView) {
            override fun onRotate(deltaAngle: Float) {
                currentAngle += deltaAngle
                if (System.currentTimeMillis() - deltaTime > timeLastSet) {
                    timeLastSet = System.currentTimeMillis()
                    mapView.mapOrientation = mapView.mapOrientation + currentAngle
                }
            }
        }

        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                InfoWindow.closeAllInfoWindowsOn(mapView)
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        })

        // Markers for each stop
        val stopMarkersOverlay = RadiusMarkerClusterer(context)
        for (stop in stopsList) {
            val marker = Marker(mapView)

            marker.position = GeoPoint(stop.latitude.toDouble(), stop.longitude.toDouble())
            marker.icon = context.getDrawable(R.drawable.ic_person_pin)
            marker.setOnMarkerClickListener { thisMarker, _ ->
                onRequestStopInfo(stop.stopId)
                coroutineScope.launch {
                    stopState.collect {
                        var textString: String = stop.title
                        if (it.predictions.isNotEmpty()) {
                            Log.e("direction", it.predictions.toString())
                            it.predictions.forEach { route ->
                                if (!route.directions.isNullOrEmpty()) {
                                    val routeDirection =
                                        route.directions[0].title.substringBefore(" ")
                                    textString += "\n" + route.routeTag + " - " + routeDirection + " in: " + route.directions[0].predictions.first().minutes + " min"
                                }
                            }
                        }
                        thisMarker.title = textString
                        thisMarker.showInfoWindow()
                    }
                }
                true
            }

            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            stopMarkersOverlay.add(marker)
        }

        val overlays =
            listOf(rotationOverlay, stopMarkersOverlay, locationOverlay, compassOverlay)
        mapView.overlays.addAll(overlays)

        // Events Overlays needs to be first to listen to events
        mapView.overlays.add(0, mapEventsOverlay)
        onLoad?.invoke(mapView)
    }
}