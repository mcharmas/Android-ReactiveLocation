package pl.charmas.android.reactivelocation2.observables.activity;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;

import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.BaseObservableOnSubscribe;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;

abstract class BaseActivityObservableOnSubscribe<T> extends BaseObservableOnSubscribe<T> {
    BaseActivityObservableOnSubscribe(ObservableContext ctx) {
        super(ctx, ActivityRecognition.API);
    }

    @Override
    protected final void onGoogleApiClientReady(Context context, GoogleApiClient googleApiClient, ObservableEmitter<? super T> emitter) {
        onActivityRecognitionClientReady(ActivityRecognition.getClient(context), emitter);
    }

    protected abstract void onActivityRecognitionClientReady(ActivityRecognitionClient activityRecognitionClient,
                                                             ObservableEmitter<? super T> emitter);
}
