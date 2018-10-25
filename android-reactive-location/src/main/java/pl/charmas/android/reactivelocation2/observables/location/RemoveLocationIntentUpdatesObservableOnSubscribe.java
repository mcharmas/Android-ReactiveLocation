package pl.charmas.android.reactivelocation2.observables.location;

import android.app.PendingIntent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import pl.charmas.android.reactivelocation2.BaseFailureListener;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;
import pl.charmas.android.reactivelocation2.observables.ObservableFactory;


public class RemoveLocationIntentUpdatesObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Void> {
    private final PendingIntent intent;

    public static Observable<Void> createObservable(ObservableContext ctx, ObservableFactory factory, PendingIntent intent) {
        return factory.createObservable(new RemoveLocationIntentUpdatesObservableOnSubscribe(ctx, intent));
    }

    private RemoveLocationIntentUpdatesObservableOnSubscribe(ObservableContext ctx, PendingIntent intent) {
        super(ctx);
        this.intent = intent;
    }

    @Override
    protected void onLocationProviderClientReady(FusedLocationProviderClient locationProviderClient,
                                                 final ObservableEmitter<? super Void> emitter) {
        locationProviderClient.removeLocationUpdates(intent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (emitter.isDisposed()) return;
                        emitter.onComplete();
                    }
                })
                .addOnFailureListener(new BaseFailureListener<>(emitter));
    }
}
