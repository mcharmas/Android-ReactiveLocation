package pl.charmas.android.reactivelocation2.observables.location;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.BaseLocationObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.StatusException;


public class RemoveLocationIntentUpdatesObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Status> {
    private final PendingIntent intent;

    public static Observable<Status> createObservable(Context ctx, PendingIntent intent) {
        return Observable.create(new RemoveLocationIntentUpdatesObservableOnSubscribe(ctx, intent));
    }

    private RemoveLocationIntentUpdatesObservableOnSubscribe(Context ctx, PendingIntent intent) {
        super(ctx);
        this.intent = intent;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<Status> emitter) {
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, intent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            emitter.onNext(status);
                            emitter.onComplete();
                        } else {
                            emitter.onError(new StatusException(status));
                        }
                    }
                });
    }
}
