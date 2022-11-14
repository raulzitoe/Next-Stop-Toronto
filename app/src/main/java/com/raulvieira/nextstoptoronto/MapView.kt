package com.raulvieira.nextstoptoronto

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
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
    stopState: StateFlow<StopPredictionModel>

) {
    val mapViewState = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AndroidView(
        factory = { mapViewState },
        modifier = modifier
    ) { mapView ->

        with(mapView.controller){
            setZoom(START_ZOOM)
            setCenter(GeoPoint(STARTING_LATITUDE, STARTING_LONGITUDE))
        }

        val compassOverlay =
            CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
        compassOverlay.enableCompass()
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        val rotationOverlay = RotationGestureOverlay(mapView)
        val overlaysList = listOf<Overlay>(compassOverlay, locationOverlay, rotationOverlay)

        with(mapView){
            overlays.addAll(overlaysList)
            setMultiTouchControls(true)
        }


        val json =
            context.resources.openRawResource(R.raw.stops).bufferedReader().use { it.readText() }
        val route = Gson().fromJson(json, RouteConfigModel::class.java)

        for (stop in route.route.stop) {
            val marker = Marker(mapView)

            marker.position = GeoPoint(stop.lat.toDouble(), stop.lon.toDouble())
            marker.setOnMarkerClickListener { marker1, mapView1 ->
                onRequestStopInfo(stop.stopId)
                coroutineScope.launch {
                    stopState.collect{
                        var textString: String = "Stop ID: " + stop.stopId
                        if (it.predictions.isNotEmpty()) {
                            Log.e("direction", it.predictions.toString())
                            it.predictions.forEach { route ->
                                if (!route.direction.isNullOrEmpty()) {
                                    textString += "\n" + route.direction[0].title + " in:" + route.direction[0].prediction.first().minutes +" min"
                                }
                            }
                        }
                        marker1.title = textString
                        marker1.showInfoWindow()
                    }
                }


                true
            }

            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
        }


        onLoad?.invoke(mapView)
    }
}