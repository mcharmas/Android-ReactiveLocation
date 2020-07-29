package pl.charmas.android.reactivelocation2.observables

import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.Api.ApiOptions.NotRequiredOptions
import com.google.android.gms.common.api.GoogleApiClient
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter

class GoogleAPIClientMaybeOnSubscribe @SafeVarargs private constructor(
    ctx: MaybeContext,
    vararg apis: Api<out NotRequiredOptions>
) : BaseMaybeOnSubscribe<GoogleApiClient>(ctx, *apis) {

    override fun onGoogleApiClientReady(
        apiClient: GoogleApiClient,
        emitter: MaybeEmitter<in GoogleApiClient>
    ) {
        if (emitter.isDisposed) return
        emitter.onSuccess(apiClient)
    }

    companion object {
        @SafeVarargs
        fun create(
            context: MaybeContext,
            factory: MaybeFactory,
            vararg apis: Api<out NotRequiredOptions>
        ): Maybe<GoogleApiClient> {
            return factory.create(
                GoogleAPIClientMaybeOnSubscribe(context, *apis)
            )
        }
    }
}