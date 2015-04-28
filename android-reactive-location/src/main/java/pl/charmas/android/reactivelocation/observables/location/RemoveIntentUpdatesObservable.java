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

public class RemoveIntentUpdatesObservable extends BaseLocationObservable<IntentUpdatesResult> {
    private final PendingIntent intent;

    public static Observable<IntentUpdatesResult> createObservable(Context ctx, PendingIntent intent) {
        return Observable.create(new RemoveIntentUpdatesObservable(ctx, intent));
    }

    private RemoveIntentUpdatesObservable(Context ctx, PendingIntent intent) {
        super(ctx);
        this.intent = intent;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super IntentUpdatesResult> observer) {
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, intent)
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
