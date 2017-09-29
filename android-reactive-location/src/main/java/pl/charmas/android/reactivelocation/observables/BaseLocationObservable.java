package pl.charmas.android.reactivelocation.observables;

import com.google.android.gms.location.LocationServices;

public abstract class BaseLocationObservable<T> extends BaseObservable<T> {
    protected BaseLocationObservable(ObservableContext ctx) {
        super(ctx, LocationServices.API);
    }
}
