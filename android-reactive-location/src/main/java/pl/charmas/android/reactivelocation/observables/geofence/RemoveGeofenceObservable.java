package pl.charmas.android.reactivelocation.observables.geofence;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;

import android.app.PendingIntent;
import android.content.Context;

import java.util.List;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public abstract class RemoveGeofenceObservable<T> extends BaseLocationObservable<T> {


    protected RemoveGeofenceObservable(Context ctx) {
        super(ctx);
    }

    public static Observable<RemoveGeofencesResult.PengingIntentRemoveGeofenceResult> createObservable(
            Context ctx, PendingIntent pendingIntent) {
        return Observable.create(new RemoveGeofenceByPendingIntentObservable(ctx, pendingIntent));
    }

    public static Observable<RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> createObservable(
            Context ctx, List<String> requestIds) {
        return Observable.create(new RemoveGeofenceRequestIdsObservable(ctx, requestIds));
    }

    @Override
    protected void onLocationClientReady(GoogleApiClient locationClient,
            final Observer<? super T> observer) {

        removeGeofences(locationClient, observer);
    }

    protected abstract void deliverResultToObserver(Result result,
            Observer<? super T> observer);

    @Override
    protected void onLocationClientDisconnected(Observer<? super T> observer) {
    }

    protected abstract void removeGeofences(GoogleApiClient googleApiClient,
            Observer<? super T> observer);
}
