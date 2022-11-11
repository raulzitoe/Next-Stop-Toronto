package com.raulvieira.nextstoptoronto

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


@Composable
fun MapView(
    modifier: Modifier = Modifier,
    onLoad: ((map: MapView) -> Unit)? = null
) {
    val mapViewState = rememberMapViewWithLifecycle()
    val context = LocalContext.current

    AndroidView(
        { mapViewState },
        modifier
    ) { mapView ->
        val mapController = mapView.controller
        mapController.setZoom(18.0)
        val startPoint = GeoPoint(43.656339, -79.460403);
        mapController.setCenter(startPoint)
        val compassOverlay =
            CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)
        mapView.setMultiTouchControls(true)
        mapView.overlays.add(RotationGestureOverlay(mapView))
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        mapView.overlays.add(locationOverlay)
        val json =
            context.resources.openRawResource(R.raw.stops).bufferedReader().use { it.readText() }
        val route = Gson().fromJson(json, RouteConfigModel::class.java)

        Log.e("risos", route.toString())
        for (stop in route.route.stop) {
            val marker = Marker(mapView)
            marker.position = GeoPoint(stop.lat.toDouble(), stop.lon.toDouble())
            marker.title = "Risos"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
        }


        onLoad?.invoke(mapView)
    }
}