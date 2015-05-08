package pl.charmas.android.reactivelocation.observables.location;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public class RemoveLocationIntentUpdatesObservable extends BaseLocationObservable<LocationUpdatesResult> {
    private final PendingIntent intent;

    public static Observable<LocationUpdatesResult> createObservable(Context ctx, PendingIntent intent) {
        return Observable.create(new RemoveLocationIntentUpdatesObservable(ctx, intent));
    }

    private RemoveLocationIntentUpdatesObservable(Context ctx, PendingIntent intent) {
        super(ctx);
        this.intent = intent;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super LocationUpdatesResult> observer) {
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, intent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        LocationUpdatesResult result = new LocationUpdatesResult(status.getStatusCode());
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
