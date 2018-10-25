package pl.charmas.android.reactivelocation2.observables;

import android.content.Context;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class GoogleAPIClientObservableOnSubscribe extends BaseObservableOnSubscribe<Void> {

    @SafeVarargs
    public static Observable<Void> create(ObservableContext context, ObservableFactory factory, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
        return factory.createObservable(new GoogleAPIClientObservableOnSubscribe(context, apis));
    }

    @SafeVarargs
    private GoogleAPIClientObservableOnSubscribe(ObservableContext ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
        super(ctx, apis);
    }

    @Override
    protected void onGoogleApiClientReady(Context context, GoogleApiClient googleApiClient, ObservableEmitter<? super Void> emitter) {
        if (emitter.isDisposed()) return;
        emitter.onComplete();
    }
}
