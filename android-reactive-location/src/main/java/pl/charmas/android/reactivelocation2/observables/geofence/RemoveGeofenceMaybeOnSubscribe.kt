package pl.charmas.android.reactivelocation2.observables.geofence

import android.app.PendingIntent
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.GeofencingClient
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import pl.charmas.android.reactivelocation2.observables.BaseLocationMaybeOnSubscribe
import pl.charmas.android.reactivelocation2.observables.MaybeContext
import pl.charmas.android.reactivelocation2.observables.MaybeFactory

abstract class RemoveGeofenceMaybeOnSubscribe<T> internal constructor(ctx: MaybeContext) :
    BaseLocationMaybeOnSubscribe<T>(ctx) {

    override fun onGoogleApiClientReady(
        apiClient: GoogleApiClient,
        emitter: MaybeEmitter<in T>
    ) {
        removeGeofences(apiClient, emitter)
    }

    protected abstract fun removeGeofences(
        locationClient: GoogleApiClient,
        emitter: MaybeEmitter<in T>
    )

    companion object {
        fun createMaybe(
            ctx: MaybeContext,
            factory: MaybeFactory,
            pendingIntent: PendingIntent,
            geofencingClient: GeofencingClient
        ): Maybe<Boolean> {
            return factory.create(
                RemoveGeofenceByPendingIntentMaybeOnSubscribe(
                    ctx,
                    pendingIntent,
                    geofencingClient
                )
            )
        }

        fun createMaybe(
            ctx: MaybeContext,
            factory: MaybeFactory,
            requestIds: List<String>,
            geofencingClient: GeofencingClient
        ): Maybe<Boolean> {
            return factory.create(
                RemoveGeofenceRequestIdsMaybeOnSubscribe(
                    ctx,
                    requestIds,
                    geofencingClient
                )
            )
        }
    }
}