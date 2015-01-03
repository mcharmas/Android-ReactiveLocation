package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

public abstract class RemoveGeofenceObservable<T> extends BaseLocationObservable<T> {

    public static Observable<RemoveGeofencesResult.PendingIntentRemoveGeofenceResult> createObservable(
            Context ctx, PendingIntent pendingIntent) {
        return Observable.create(new RemoveGeofenceByPendingIntentObservable(ctx, pendingIntent));
    }

    public static Observable<RemoveGeofencesResult.RequestIdsRemoveGeofenceResult> createObservable(
            Context ctx, List<String> requestIds) {
        return Observable.create(new RemoveGeofenceRequestIdsObservable(ctx, requestIds));
    }

    protected RemoveGeofenceObservable(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super T> observer) {
        removeGeofences(apiClient, observer);
    }

    protected abstract void removeGeofences(GoogleApiClient locationClient, Observer<? super T> observer);

}
