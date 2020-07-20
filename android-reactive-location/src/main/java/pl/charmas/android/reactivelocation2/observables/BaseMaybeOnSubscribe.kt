package pl.charmas.android.reactivelocation2.observables

import android.content.Context
import android.os.Bundle
import android.os.Handler
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.Api.ApiOptions.NotRequiredOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe
import io.reactivex.disposables.Disposables
import pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionException
import pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionSuspendedException

abstract class BaseMaybeOnSubscribe<T> @SafeVarargs protected constructor(
    ctx: MaybeContext,
    vararg services: Api<out NotRequiredOptions>
) : MaybeOnSubscribe<T> {

    private val ctx: Context = ctx.context
    private val handler: Handler? = ctx.handler
    private val services: List<Api<out NotRequiredOptions>> = listOf(*services)

    override fun subscribe(emitter: MaybeEmitter<T>) {
        val apiClient = createApiClient(emitter)
        try {
            apiClient.connect()
        } catch (ex: Throwable) {
            if (!emitter.isDisposed) {
                emitter.onError(ex)
            }
        }
        emitter.setDisposable(Disposables.fromAction {
            onDisposed(apiClient)
            apiClient.disconnect()
        })
    }

    private fun createApiClient(emitter: MaybeEmitter<in T>): GoogleApiClient {
        val apiClientConnectionCallbacks = ApiClientConnectionCallbacks(emitter)

        val builder = GoogleApiClient.Builder(ctx)
        for (service in services) {
            builder.addApi(service)
        }
        builder
            .addConnectionCallbacks(apiClientConnectionCallbacks)
            .addOnConnectionFailedListener(apiClientConnectionCallbacks)
        if (handler != null) {
            builder.setHandler(handler)
        }
        val apiClient = builder.build()
        apiClientConnectionCallbacks.setClient(apiClient)
        return apiClient
    }

    private fun onDisposed(locationClient: GoogleApiClient?) {}

    protected abstract fun onGoogleApiClientReady(
        apiClient: GoogleApiClient,
        emitter: MaybeEmitter<in T>
    )

    private inner class ApiClientConnectionCallbacks constructor(private val emitter: MaybeEmitter<in T>) :
        ConnectionCallbacks, OnConnectionFailedListener {
        private lateinit var apiClient: GoogleApiClient

        override fun onConnected(bundle: Bundle?) {
            try {
                onGoogleApiClientReady(apiClient, emitter)
            } catch (ex: Throwable) {
                if (!emitter.isDisposed) {
                    emitter.onError(ex)
                }
            }
        }

        override fun onConnectionSuspended(cause: Int) {
            if (!emitter.isDisposed) {
                emitter.onError(GoogleAPIConnectionSuspendedException(cause))
            }
        }

        override fun onConnectionFailed(connectionResult: ConnectionResult) {
            if (!emitter.isDisposed) {
                emitter.onError(
                    GoogleAPIConnectionException(
                        "Error connecting to GoogleApiClient.",
                        connectionResult
                    )
                )
            }
        }

        fun setClient(client: GoogleApiClient) {
            apiClient = client
        }
    }
}