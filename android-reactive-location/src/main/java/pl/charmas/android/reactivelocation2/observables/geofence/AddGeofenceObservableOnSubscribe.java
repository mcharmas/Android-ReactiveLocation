package pl.charmas.android.reactivelocation2.observables.geofence;

import android.app.PendingIntent;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.tasks.OnSuccessListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.BaseFailureListener;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;
import pl.charmas.android.reactivelocation2.observables.ObservableFactory;


@SuppressWarnings("MissingPermission")
public class AddGeofenceObservableOnSubscribe extends BaseGeofencingObservableOnSubscribe<Void> {
    private final GeofencingRequest request;
    private final PendingIntent geofenceTransitionPendingIntent;

    public static Observable<Void> createObservable(ObservableContext ctx, ObservableFactory factory, GeofencingRequest request, PendingIntent
            geofenceTransitionPendingIntent) {
        return factory.createObservable(new AddGeofenceObservableOnSubscribe(ctx, request, geofenceTransitionPendingIntent));
    }

    private AddGeofenceObservableOnSubscribe(ObservableContext ctx, GeofencingRequest request, PendingIntent geofenceTransitionPendingIntent) {
        super(ctx);
        this.request = request;
        this.geofenceTransitionPendingIntent = geofenceTransitionPendingIntent;
    }

    @Override
    protected void onGeofencingClientReady(GeofencingClient geofencingClient, final ObservableEmitter<? super Void> emitter) {
        geofencingClient.addGeofences(request, geofenceTransitionPendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        emitter.onComplete();
                    }
                })
                .addOnFailureListener(new BaseFailureListener<>(emitter));
    }

}
