package pl.charmas.android.reactivelocation2.observables.geofence;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;

import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.BaseObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;

public abstract class BaseGeofencingObservableOnSubscribe<T> extends BaseObservableOnSubscribe<T> {

    protected BaseGeofencingObservableOnSubscribe(ObservableContext ctx) {
        super(ctx, LocationServices.API);
    }

    @Override
    protected final void onGoogleApiClientReady(Context context, GoogleApiClient googleApiClient, ObservableEmitter<? super T> emitter) {
        onGeofencingClientReady(LocationServices.getGeofencingClient(context), emitter);
    }

    protected abstract void onGeofencingClientReady(GeofencingClient geofencingClient,
                                                    ObservableEmitter<? super T> emitter);
}
