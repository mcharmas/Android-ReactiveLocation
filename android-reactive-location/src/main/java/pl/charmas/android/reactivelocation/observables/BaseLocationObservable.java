package pl.charmas.android.reactivelocation.observables;

import android.content.Context;

import com.google.android.gms.location.LocationServices;

public abstract class BaseLocationObservable<T> extends BaseObservable<T> {
    protected BaseLocationObservable(Context ctx) {
        super(ctx, LocationServices.API);
    }
}
