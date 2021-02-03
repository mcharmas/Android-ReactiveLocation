package pl.charmas.android.reactivelocation2.observables.geofence

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.GeofencingClient
import io.reactivex.MaybeEmitter
import pl.charmas.android.reactivelocation2.observables.MaybeContext

internal class RemoveGeofenceRequestIdsMaybeOnSubscribe constructor(
    ctx: MaybeContext,
    private val geofenceRequestIds: List<String>,
    private val geofencingClient: GeofencingClient
) : RemoveGeofenceMaybeOnSubscribe<Boolean>(ctx) {

    override fun removeGeofences(
        locationClient: GoogleApiClient,
        emitter: MaybeEmitter<in Boolean>
    ) {
        geofencingClient.removeGeofences(geofenceRequestIds)
            .addOnSuccessListener {
                if (!emitter.isDisposed) {
                    emitter.onSuccess(true)
                }
            }
            .addOnFailureListener { error ->
                if (!emitter.isDisposed) {
                    emitter.onError(error)
                }
            }
    }
}