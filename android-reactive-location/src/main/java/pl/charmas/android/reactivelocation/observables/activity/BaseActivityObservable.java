package pl.charmas.android.reactivelocation.observables.activity;


import com.google.android.gms.location.ActivityRecognition;

import pl.charmas.android.reactivelocation.observables.BaseObservable;
import pl.charmas.android.reactivelocation.observables.ObservableContext;

abstract class BaseActivityObservable<T> extends BaseObservable<T> {
    BaseActivityObservable(ObservableContext ctx) {
        super(ctx, ActivityRecognition.API);
    }
}
