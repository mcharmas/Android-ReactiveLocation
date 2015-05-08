package pl.charmas.android.reactivelocation.observables.location;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class AddIntentUpdatesObservable extends BaseLocationObservable<IntentUpdatesResult> {
    private final LocationRequest locationRequest;
    private final PendingIntent intent;

    public static Observable<IntentUpdatesResult> createObservable(Context ctx, LocationRequest locationRequest, PendingIntent intent) {
        return Observable.create(new AddIntentUpdatesObservable(ctx, locationRequest, intent));
    }

    private AddIntentUpdatesObservable(Context ctx, LocationRequest locationRequest, PendingIntent intent) {
        super(ctx);
        this.locationRequest = locationRequest;
        this.intent = intent;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super IntentUpdatesResult> observer) {
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, intent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        IntentUpdatesResult result = new IntentUpdatesResult(status.getStatusCode());
                        if (!result.isSuccess()) {
                            observer.onError(new IntentUpdatesException(result));
                        } else {
                            observer.onNext(result);
                            observer.onCompleted();
                        }
                    }
                });

    }
}
