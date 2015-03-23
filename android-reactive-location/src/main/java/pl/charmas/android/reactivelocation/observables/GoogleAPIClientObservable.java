package pl.charmas.android.reactivelocation.observables;

import android.content.Context;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import rx.Observable;
import rx.Observer;

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
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super GoogleApiClient> observer) {
        observer.onNext(apiClient);
        observer.onCompleted();
    }
}
