package pl.charmas.android.reactivelocation;

import android.content.Context;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import pl.charmas.android.reactivelocation.observables.GoogleAPIClientObservable;
import rx.Observable;
import rx.Observer;

class MockModeObservable extends GoogleAPIClientObservable {

    @SafeVarargs
    public static Observable<GoogleApiClient> create(Context context, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
        return Observable.create(new MockModeObservable(context, apis));
    }

    @SafeVarargs
    protected MockModeObservable(Context ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
        super(ctx, apis);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super GoogleApiClient> observer) {
        LocationServices.FusedLocationApi.setMockMode(apiClient, true);
        super.onGoogleApiClientReady(apiClient, observer);
    }

    @Override
    protected void onUnsubscribed(GoogleApiClient locationClient) {
        if (locationClient.isConnected()) {
            try {
                LocationServices.FusedLocationApi.setMockMode(locationClient, false);
            } catch (SecurityException e) {
                // if this happens then we couldn't have switched mock mode on in the first place,
                // and the observer's onError will already have been called from BaseObservable
            }
        }
    }
}
