package pl.charmas.android.reactivelocation2.observables.location;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.BaseObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;

public abstract class BaseLocationObservableOnSubscribe<T> extends BaseObservableOnSubscribe<T> {

    protected BaseLocationObservableOnSubscribe(ObservableContext ctx) {
        super(ctx, LocationServices.API);
    }

    @Override
    protected final void onGoogleApiClientReady(Context context, GoogleApiClient googleApiClient, ObservableEmitter<? super T> emitter) {
        onLocationProviderClientReady(LocationServices.getFusedLocationProviderClient(context), emitter);
    }

    protected abstract void onLocationProviderClientReady(FusedLocationProviderClient locationProviderClient,
                                                          ObservableEmitter<? super T> emitter);
}
