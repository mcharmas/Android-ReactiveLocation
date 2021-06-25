package pl.charmas.android.reactivelocation2.observables.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import pl.charmas.android.reactivelocation2.observables.ObservableContext;
import pl.charmas.android.reactivelocation2.observables.ObservableFactory;


@SuppressWarnings("MissingPermission")
public class ActivityUpdatesObservableOnSubscribe extends BaseActivityObservableOnSubscribe<ActivityRecognitionResult> {
    private static final String ACTION_ACTIVITY_DETECTED = "pl.charmas.android.reactivelocation2.ACTION_ACTIVITY_UPDATE_DETECTED";

    private final Context context;
    private final int detectionIntervalMilliseconds;
    private ActivityUpdatesBroadcastReceiver receiver;

    public static Observable<ActivityRecognitionResult> createObservable(ObservableContext ctx, ObservableFactory factory, int detectionIntervalMiliseconds) {
        return factory.createObservable(new ActivityUpdatesObservableOnSubscribe(ctx, detectionIntervalMiliseconds));
    }

    private ActivityUpdatesObservableOnSubscribe(ObservableContext context, int detectionIntervalMilliseconds) {
        super(context);
        this.context = context.getContext();
        this.detectionIntervalMilliseconds = detectionIntervalMilliseconds;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, ObservableEmitter<? super ActivityRecognitionResult> emitter) {
        receiver = new ActivityUpdatesBroadcastReceiver(emitter);
        context.registerReceiver(receiver, new IntentFilter(ACTION_ACTIVITY_DETECTED));
        PendingIntent receiverIntent = getReceiverPendingIntent();
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, detectionIntervalMilliseconds, receiverIntent);
    }

    private PendingIntent getReceiverPendingIntent() {
        return PendingIntent.getBroadcast(context, 0, new Intent(ACTION_ACTIVITY_DETECTED), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onDisposed(GoogleApiClient apiClient) {
        if (apiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(apiClient, getReceiverPendingIntent());
        }
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private static class ActivityUpdatesBroadcastReceiver extends BroadcastReceiver {
        private final ObservableEmitter<? super ActivityRecognitionResult> emitter;

        public ActivityUpdatesBroadcastReceiver(ObservableEmitter<? super ActivityRecognitionResult> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                emitter.onNext(result);
            }
        }
    }
}
