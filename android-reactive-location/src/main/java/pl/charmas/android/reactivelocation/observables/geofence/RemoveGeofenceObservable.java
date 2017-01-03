package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;


public abstract class RemoveGeofenceObservable<T> extends BaseLocationObservable<T> {

    public static Observable<Status> createObservable(
            Context ctx, PendingIntent pendingIntent) {
        return Observable.create(new RemoveGeofenceByPendingIntentObservable(ctx, pendingIntent));
    }

    public static Observable<Status> createObservable(
            Context ctx, List<String> requestIds) {
        return Observable.create(new RemoveGeofenceRequestIdsObservable(ctx, requestIds));
    }

    protected RemoveGeofenceObservable(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<T> observer) {
        removeGeofences(apiClient, observer);
    }

    protected abstract void removeGeofences(GoogleApiClient locationClient, ObservableEmitter<T> observer);

}
