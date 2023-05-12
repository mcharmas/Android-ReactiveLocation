package pl.charmas.android.reactivelocation2.observables.geofence;

import android.app.PendingIntent;

import com.google.android.gms.location.GeofencingClient;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;
import pl.charmas.android.reactivelocation2.observables.ObservableFactory;


public abstract class RemoveGeofenceObservableOnSubscribe<T> extends BaseGeofencingObservableOnSubscribe<T> {

    public static Observable<Void> createObservable(ObservableContext ctx, ObservableFactory factory, PendingIntent pendingIntent) {
        return factory.createObservable(new RemoveGeofenceByPendingIntentObservableOnSubscribe(ctx, pendingIntent));
    }

    public static Observable<Void> createObservable(ObservableContext ctx, ObservableFactory factory, List<String> requestIds) {
        return factory.createObservable(new RemoveGeofenceRequestIdsObservableOnSubscribe(ctx, requestIds));
    }

    RemoveGeofenceObservableOnSubscribe(ObservableContext ctx) {
        super(ctx);
    }

    @Override
    protected void onGeofencingClientReady(GeofencingClient geofencingClient, final ObservableEmitter<? super T> emitter) {
        removeGeofences(geofencingClient, emitter);
    }

    protected abstract void removeGeofences(GeofencingClient geofencingClient, ObservableEmitter<? super T> emitter);

}
