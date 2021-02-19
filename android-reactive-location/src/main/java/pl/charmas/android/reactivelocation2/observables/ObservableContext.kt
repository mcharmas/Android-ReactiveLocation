package pl.charmas.android.reactivelocation2.observables

import android.content.Context
import android.os.Handler
import pl.charmas.android.reactivelocation2.ReactiveLocationProviderConfiguration

class ObservableContext constructor(
    val context: Context,
    configuration: ReactiveLocationProviderConfiguration
) {
    val handler: Handler? = configuration.customCallbackHandler
    val isRetryOnConnectionSuspended: Boolean = configuration.isRetryOnConnectionSuspended
}