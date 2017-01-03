package pl.charmas.android.reactivelocation.observables;

import android.content.Context;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;


public class GoogleAPIClientObservable extends BaseObservable<GoogleApiClient> {

    @SafeVarargs
    public static Observable<GoogleApiClient> create(Context context, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
        return Observable.create(new GoogleAPIClientObservable(context, apis));
    }

    @SafeVarargs
    protected GoogleAPIClientObservable(Context ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
        super(ctx, apis);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, ObservableEmitter<GoogleApiClient> observer) {
        observer.onNext(apiClient);
        observer.onComplete();
    }
}
