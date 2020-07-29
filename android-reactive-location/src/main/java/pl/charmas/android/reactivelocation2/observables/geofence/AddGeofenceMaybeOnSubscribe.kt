package pl.charmas.android.reactivelocation2.observables.geofence

import android.app.PendingIntent
import androidx.annotation.RequiresPermission
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import pl.charmas.android.reactivelocation2.observables.BaseLocationMaybeOnSubscribe
import pl.charmas.android.reactivelocation2.observables.MaybeContext
import pl.charmas.android.reactivelocation2.observables.MaybeFactory

class AddGeofenceMaybeOnSubscribe private constructor(
    ctx: MaybeContext,
    private val request: GeofencingRequest,
    private val geofenceTransitionPendingIntent: PendingIntent,
    private val geofencingClient: GeofencingClient
) : BaseLocationMaybeOnSubscribe<Boolean>(ctx) {

    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    override fun onGoogleApiClientReady(
        apiClient: GoogleApiClient,
        emitter: MaybeEmitter<in Boolean>
    ) {
        geofencingClient.addGeofences(request, geofenceTransitionPendingIntent)
            .addOnSuccessListener { sd: Void? ->
                if (emitter.isDisposed) return@addOnSuccessListener
                emitter.onSuccess(true)
            }
            .addOnFailureListener { error: Exception? ->
                if (emitter.isDisposed) return@addOnFailureListener
                emitter.onError(error!!)
            }
    }

    companion object {
        fun createMaybe(
            geofencingClient: GeofencingClient,
            ctx: MaybeContext,
            factory: MaybeFactory,
            request: GeofencingRequest,
            geofenceTransitionPendingIntent: PendingIntent
        ): Maybe<Boolean> {
            return factory.create(
                AddGeofenceMaybeOnSubscribe(
                    ctx,
                    request,
                    geofenceTransitionPendingIntent,
                    geofencingClient
                )
            )
        }
    }
}