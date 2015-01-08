package pl.charmas.android.reactivelocation.observables.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import pl.charmas.android.reactivelocation.observables.BaseActivityObservable;
import rx.Observable;
import rx.Observer;

public class ActivityUpdatesObservable extends BaseActivityObservable<DetectedActivity> {

    private static final String TAG = ActivityUpdatesObservable.class.getSimpleName();

    public static final String ACTION_ACTIVITY_DETECTED = "action_activity_detected";

    private PendingIntent mActivityRecognitionPendingIntent;

    public static Observable<DetectedActivity> createObservable(Context ctx, int detectionIntervalMiliseconds) {
        return Observable.create(new ActivityUpdatesObservable(ctx, detectionIntervalMiliseconds));
    }

    private final Context ctx;
    private final int detectionIntervalMilliseconds;
    private Observer<? super DetectedActivity> mObserver;


    private ActivityUpdatesObservable(Context ctx, int detectionIntervalMilliseconds) {
        super(ctx);
        this.ctx = ctx;
        this.detectionIntervalMilliseconds = detectionIntervalMilliseconds;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super DetectedActivity> observer) {
        mObserver = observer;

        PendingIntent pIntent = createRequestPendingIntent();
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, detectionIntervalMilliseconds, pIntent);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ACTIVITY_DETECTED);


        LocalBroadcastManager.getInstance(ctx).registerReceiver(activityReceiver, intentFilter);
    }

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {

                // Get the update
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

                DetectedActivity mostProbableActivity = result.getMostProbableActivity();


                if (mObserver != null) {
                    mObserver.onNext(mostProbableActivity);
                }

            }
        }
    };

    @Override
    protected void onUnsubscribed(GoogleApiClient apiClient) {

        if (/*apiClient.isConnected() && */mActivityRecognitionPendingIntent != null) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(apiClient, mActivityRecognitionPendingIntent);
            mActivityRecognitionPendingIntent.cancel();
        }

        if (activityReceiver != null) {
            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(activityReceiver);
        }

        mActivityRecognitionPendingIntent = null;
        activityReceiver = null;
        mObserver = null;
    }


    /**
     * Get a PendingIntent to send with the request to get activity recognition updates. Location
     * Services issues the Intent inside this PendingIntent whenever a activity recognition update
     * occurs.
     *
     * @return A PendingIntent for the IntentService that handles activity recognition updates.
     */
    private PendingIntent createRequestPendingIntent() {

        // If the PendingIntent already exists
        if (null != mActivityRecognitionPendingIntent) {

            // Return the existing intent
            return mActivityRecognitionPendingIntent;

            // If no PendingIntent exists
        } else {


            // Create an Intent pointing to the IntentService
            Intent intent = new Intent(ctx, ActivityRecognitionIntentService.class);

            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
            PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);



            mActivityRecognitionPendingIntent = pendingIntent;
            return mActivityRecognitionPendingIntent;



        }


    }
}
