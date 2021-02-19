package pl.charmas.android.reactivelocation2.observables.geofence

import android.app.PendingIntent
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.GeofencingClient
import io.reactivex.MaybeEmitter
import pl.charmas.android.reactivelocation2.observables.MaybeContext

class RemoveGeofenceByPendingIntentMaybeOnSubscribe(
    ctx: MaybeContext,
    private val pendingIntent: PendingIntent,
    private val geofencingClient: GeofencingClient
) : RemoveGeofenceMaybeOnSubscribe<Boolean>(ctx) {

    override fun removeGeofences(
        locationClient: GoogleApiClient,
        emitter: MaybeEmitter<in Boolean>
    ) {
        geofencingClient.removeGeofences(pendingIntent)
            .addOnSuccessListener {
                if (emitter.isDisposed) return@addOnSuccessListener
                emitter.onSuccess(true)
            }
            .addOnFailureListener { error ->
                if (emitter.isDisposed) return@addOnFailureListener
                emitter.onError(error)
            }
    }
}