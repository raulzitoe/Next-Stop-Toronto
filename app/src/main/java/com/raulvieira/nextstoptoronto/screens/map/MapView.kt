package com.raulvieira.nextstoptoronto.screens.map


import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.GeometryMath
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

        val mapNorthCompassOverlay = object : CompassOverlay(context, mapView) {
            override fun draw(c: Canvas?, pProjection: Projection?) {
                drawCompass(c, -mapView.mapOrientation, pProjection?.screenRect)
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
        val stopMarkersOverlay = filteredMarkersOverlay(
            context,
            stopsList,
            mapView,
            onRequestStopInfo = { stopId -> onRequestStopInfo(stopId) },
            stopState,
            coroutineScope
        )


        val overlays =
            listOf(rotationOverlay, stopMarkersOverlay, locationOverlay, mapNorthCompassOverlay)
        mapView.overlays.addAll(overlays)

        mapView.setOnTouchListener { view, motionEvent ->
            view.performClick()
            if (motionEvent.actionMasked == MotionEvent.ACTION_UP) {
                val stops = filteredMarkersOverlay(
                    context,
                    stopsList,
                    mapView,
                    onRequestStopInfo = { stopId -> onRequestStopInfo(stopId) },
                    stopState,
                    coroutineScope
                )
                (view as MapView).overlays[2] = stops
                view.invalidate()
            }
            false
        }

        // Events Overlays needs to be first to listen to events
        mapView.overlays.add(0, mapEventsOverlay)
        onLoad?.invoke(mapView)
    }
}

fun isStopWithinBoundBox(
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

fun filteredMarkersOverlay(
    context: Context,
    stopsList: List<StopModel>,
    mapView: MapView,
    onRequestStopInfo: (stopId: String) -> Unit,
    stopState: StateFlow<StopPredictionModel>,
    coroutineScope: CoroutineScope
): RadiusMarkerClusterer {
    val stopMarkersOverlay = RadiusMarkerClusterer(context)
    for (stop in stopsList) {
        if (!isStopWithinBoundBox(
                stop.latitude.toDouble(),
                stop.longitude.toDouble(),
                mapView.boundingBox
            )
        ) continue

        val marker = Marker(mapView)

        marker.position = GeoPoint(stop.latitude.toDouble(), stop.longitude.toDouble())
        marker.icon = context.getDrawable(R.drawable.ic_person_pin)
        marker.setOnMarkerClickListener { thisMarker, _ ->
            onRequestStopInfo(stop.stopId)
            coroutineScope.launch {
                stopState.collect {
                    var textString: String = stop.title
                    if (it.predictions.isNotEmpty()) {
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
    return stopMarkersOverlay
}