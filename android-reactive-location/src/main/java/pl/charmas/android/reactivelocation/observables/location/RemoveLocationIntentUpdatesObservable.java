package pl.charmas.android.reactivelocation.observables.location;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import pl.charmas.android.reactivelocation.observables.StatusException;


public class RemoveLocationIntentUpdatesObservable extends BaseLocationObservable<Status> {
    private final PendingIntent intent;

    public static Observable<Status> createObservable(Context ctx, PendingIntent intent) {
        return Observable.create(new RemoveLocationIntentUpdatesObservable(ctx, intent));
    }

    private RemoveLocationIntentUpdatesObservable(Context ctx, PendingIntent intent) {
        super(ctx);
        this.intent = intent;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<Status> observer) {
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, intent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            observer.onNext(status);
                            observer.onComplete();
                        } else {
                            observer.onError(new StatusException(status));
                        }
                    }
                });
    }
}
