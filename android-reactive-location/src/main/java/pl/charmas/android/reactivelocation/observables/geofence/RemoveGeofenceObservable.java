package pl.charmas.android.reactivelocation.observables.geofence;

import android.app.PendingIntent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import java.util.List;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import pl.charmas.android.reactivelocation.observables.ObservableContext;
import rx.Observable;
import rx.Observer;

public abstract class RemoveGeofenceObservable<T> extends BaseLocationObservable<T> {

    public static Observable<Status> createObservable(ObservableContext ctx, PendingIntent pendingIntent) {
        return Observable.create(new RemoveGeofenceByPendingIntentObservable(ctx, pendingIntent));
    }

    public static Observable<Status> createObservable(ObservableContext ctx, List<String> requestIds) {
        return Observable.create(new RemoveGeofenceRequestIdsObservable(ctx, requestIds));
    }

    RemoveGeofenceObservable(ObservableContext ctx) {
        super(ctx);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super T> observer) {
        removeGeofences(apiClient, observer);
    }

    protected abstract void removeGeofences(GoogleApiClient locationClient, Observer<? super T> observer);

}
