package pl.charmas.android.reactivelocation.observables;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

import rx.Observable;
import rx.Observer;

public class ApiClientObservable extends BaseLocationObservable<GoogleApiClient> {

    public static Observable<GoogleApiClient> create(Context context) {
        return Observable.create(new ApiClientObservable(context));
    }

    protected ApiClientObservable(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super GoogleApiClient> observer) {
        observer.onNext(apiClient);
        observer.onCompleted();
    }
}
