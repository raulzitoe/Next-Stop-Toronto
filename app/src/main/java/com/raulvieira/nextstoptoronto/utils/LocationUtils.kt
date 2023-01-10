package com.raulvieira.nextstoptoronto.utils

import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

fun createLocationRequest() = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
    .setWaitForAccurateLocation(false)
    .setMinUpdateIntervalMillis(3000)
    .setMaxUpdateDelayMillis(5000)
    .build()

//@OptIn(ExperimentalCoroutinesApi::class)
//@SuppressLint("MissingPermission")
//suspend fun FusedLocationProviderClient.awaitLastLocation(): Location =
//    suspendCancellableCoroutine<Location> { continuation ->
//        lastLocation.addOnSuccessListener { location ->
//            continuation.resume(value = location, onCancellation = {})
//        }.addOnFailureListener { e ->
//            continuation.resumeWithException(e)
//        }
//    }

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locationFlow(externalScope: CoroutineScope) = callbackFlow {
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            try {
                trySendBlocking(result.lastLocation) // emit location into the Flow using ProducerScope.offer
            } catch (e: Exception) {
                // nothing to do
                // Channel was probably already closed by the time offer was called
            }
        }
    }

    requestLocationUpdates(
        createLocationRequest(),
        callback,
        Looper.getMainLooper()
    ).addOnFailureListener { e ->
        close(e) // in case of exception, close the Flow
    }

    awaitClose {
        removeLocationUpdates(callback) // clean up when Flow collection ends
    }
}.shareIn(
    externalScope,
    replay = 0,
    started = SharingStarted.WhileSubscribed()
)