package pl.charmas.android.reactivelocation2.observables;

import android.content.Context;

import com.google.android.gms.location.LocationServices;

public abstract class BaseLocationObservableOnSubscribe<T> extends BaseObservableOnSubscribe<T> {
    protected BaseLocationObservableOnSubscribe(Context ctx) {
        super(ctx, LocationServices.API);
    }
}
