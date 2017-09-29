package pl.charmas.android.reactivelocation2.observables.geofence;

import android.app.PendingIntent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.BaseLocationObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;


public abstract class RemoveGeofenceObservableOnSubscribe<T> extends BaseLocationObservableOnSubscribe<T> {

    public static Observable<Status> createObservable(ObservableContext ctx, PendingIntent pendingIntent) {
        return Observable.create(new RemoveGeofenceByPendingIntentObservableOnSubscribe(ctx, pendingIntent));
    }

    public static Observable<Status> createObservable(ObservableContext ctx, List<String> requestIds) {
        return Observable.create(new RemoveGeofenceRequestIdsObservableOnSubscribe(ctx, requestIds));
    }

    RemoveGeofenceObservableOnSubscribe(ObservableContext ctx) {
        super(ctx);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final ObservableEmitter<? super T> emitter) {
        removeGeofences(apiClient, emitter);
    }

    protected abstract void removeGeofences(GoogleApiClient locationClient, ObservableEmitter<? super T> emitter);

}
