package pl.charmas.android.reactivelocation2.observables.activity;

import android.content.Context;

import com.google.android.gms.location.ActivityRecognition;

import pl.charmas.android.reactivelocation2.observables.BaseObservableOnSubscribe;

abstract class BaseActivityObservableOnSubscribe<T> extends BaseObservableOnSubscribe<T> {
    BaseActivityObservableOnSubscribe(Context ctx) {
        super(ctx, ActivityRecognition.API);
    }
}
