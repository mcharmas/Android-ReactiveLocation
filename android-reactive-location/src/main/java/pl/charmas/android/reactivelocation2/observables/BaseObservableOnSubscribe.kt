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
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposables
import pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionException
import pl.charmas.android.reactivelocation2.observables.exceptions.GoogleAPIConnectionSuspendedException
import java.util.Arrays

abstract class BaseObservableOnSubscribe<T> @SafeVarargs protected constructor(
    ctx: ObservableContext,
    vararg services: Api<out NotRequiredOptions>
) : ObservableOnSubscribe<T> {

    private val ctx: Context= ctx.context
    private val handler: Handler? = ctx.handler
    private val services: List<Api<out NotRequiredOptions>> = listOf(*services)

    override fun subscribe(emitter: ObservableEmitter<T>) {
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

    private fun createApiClient(emitter: ObservableEmitter<in T>): GoogleApiClient {
        val apiClientConnectionCallbacks =
            ApiClientConnectionCallbacks(
                emitter
            )
        var apiClientBuilder = GoogleApiClient.Builder(ctx)
        for (service in services) {
            apiClientBuilder = apiClientBuilder.addApi(service)
        }
        apiClientBuilder = apiClientBuilder
            .addConnectionCallbacks(apiClientConnectionCallbacks)
            .addOnConnectionFailedListener(apiClientConnectionCallbacks)
        if (handler != null) {
            apiClientBuilder = apiClientBuilder.setHandler(handler)
        }
        val apiClient = apiClientBuilder.build()
        apiClientConnectionCallbacks.setClient(apiClient)
        return apiClient
    }

    protected open fun onDisposed(locationClient: GoogleApiClient?) {}
    protected abstract fun onGoogleApiClientReady(
        apiClient: GoogleApiClient?,
        emitter: ObservableEmitter<in T>?
    )

    private inner class ApiClientConnectionCallbacks  constructor(private val emitter: ObservableEmitter<in T>) :
        ConnectionCallbacks, OnConnectionFailedListener {
        private var apiClient: GoogleApiClient? = null
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

        fun setClient(client: GoogleApiClient?) {
            apiClient = client
        }
    }

    init {
        this.ctx
        handler
        this.services
    }
}