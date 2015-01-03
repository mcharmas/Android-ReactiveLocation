package pl.charmas.android.reactivelocation.observables;

import android.content.Context;

import com.google.android.gms.location.ActivityRecognition;


public abstract class BaseActivityObservable<T> extends BaseObservable<T> {


    protected BaseActivityObservable(Context ctx) {
        super(ctx, ActivityRecognition.API);
    }

}
