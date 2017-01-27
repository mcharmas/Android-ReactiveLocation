package pl.charmas.android.reactivelocation2.observables.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.BaseLocationObservableOnSubscribe;


public abstract class RemoveGeofenceObservableOnSubscribe<T> extends BaseLocationObservableOnSubscribe<T> {

    public static Observable<Status> createObservable(Context ctx, PendingIntent pendingIntent) {
        return Observable.create(new RemoveGeofenceByPendingIntentObservableOnSubscribe(ctx, pendingIntent));
    }

    public static Observable<Status> createObservable(Context ctx, List<String> requestIds) {
        return Observable.create(new RemoveGeofenceRequestIdsObservableOnSubscribe(ctx, requestIds));
    }

    RemoveGeofenceObservableOnSubscribe(Context ctx) {
        super(ctx);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<T> emitter) {
        removeGeofences(apiClient, emitter);
    }

    protected abstract void removeGeofences(GoogleApiClient locationClient, ObservableEmitter<T> emitter);

}
