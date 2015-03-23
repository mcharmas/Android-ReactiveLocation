package pl.charmas.android.reactivelocation.observables.activity;

import android.content.Context;

import com.google.android.gms.location.ActivityRecognition;

import pl.charmas.android.reactivelocation.observables.BaseObservable;


public abstract class BaseActivityObservable<T> extends BaseObservable<T> {
    protected BaseActivityObservable(Context ctx) {
        super(ctx, ActivityRecognition.API);
    }
}
