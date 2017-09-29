package pl.charmas.android.reactivelocation2.observables.activity;

import com.google.android.gms.location.ActivityRecognition;

import pl.charmas.android.reactivelocation2.observables.BaseObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;

abstract class BaseActivityObservableOnSubscribe<T> extends BaseObservableOnSubscribe<T> {
    BaseActivityObservableOnSubscribe(ObservableContext ctx) {
        super(ctx, ActivityRecognition.API);
    }
}
