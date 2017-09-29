package pl.charmas.android.reactivelocation2.observables;

import com.google.android.gms.location.LocationServices;

public abstract class BaseLocationObservableOnSubscribe<T> extends BaseObservableOnSubscribe<T> {
    protected BaseLocationObservableOnSubscribe(ObservableContext ctx) {
        super(ctx, LocationServices.API);
    }
}
