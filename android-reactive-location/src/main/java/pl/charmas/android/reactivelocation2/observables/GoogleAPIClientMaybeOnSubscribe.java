package pl.charmas.android.reactivelocation2.observables;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;

public class GoogleAPIClientMaybeOnSubscribe extends BaseMaybeOnSubscribe<GoogleApiClient> {

    @SafeVarargs
    public static Maybe<GoogleApiClient> create(MaybeContext context, MaybeFactory factory, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
        return factory.createMaybe(new GoogleAPIClientMaybeOnSubscribe(context, apis));
    }

    @SafeVarargs
    private GoogleAPIClientMaybeOnSubscribe(MaybeContext ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
        super(ctx, apis);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, MaybeEmitter<? super GoogleApiClient> emitter) {
        if (emitter.isDisposed()) return;
        emitter.onSuccess(apiClient);
    }
}
