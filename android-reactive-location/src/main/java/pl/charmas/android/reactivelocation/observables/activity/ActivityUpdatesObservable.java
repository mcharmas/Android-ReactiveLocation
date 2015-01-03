package pl.charmas.android.reactivelocation.observables.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import pl.charmas.android.reactivelocation.observables.BaseActivityObservable;
import rx.Observable;
import rx.Observer;

public class ActivityUpdatesObservable extends BaseActivityObservable<DetectedActivity> {

    private static final String TAG = ActivityUpdatesObservable.class.getSimpleName();

    private PendingIntent mActivityRecognitionPendingIntent;

    public static Observable<DetectedActivity> createObservable(Context ctx, int detectionIntervalMiliseconds) {
        return Observable.create(new ActivityUpdatesObservable(ctx, detectionIntervalMiliseconds));
    }

    private final Context ctx;
    private final int detectionIntervalMiliseconds;
    private static Observer<? super DetectedActivity> mObserver;

    public static Observer<? super DetectedActivity> getObserver() {
        return mObserver;
    }

    private static void setObserver(ActivityUpdatesObservable activityUpdatesObservable, GoogleApiClient apiClient, Observer<? super DetectedActivity> observer) {
        if (getObserver() != null) {
            /*
             * Unsubscribe previous observer to avoid memory leak
             */
            activityUpdatesObservable.onUnsubscribed(apiClient);
        }
        mObserver = observer;
    }

    private ActivityUpdatesObservable(Context ctx, int detectionIntervalMiliseconds) {
        super(ctx);
        this.ctx = ctx;
        this.detectionIntervalMiliseconds = detectionIntervalMiliseconds;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super DetectedActivity> observer) {
        setObserver(this, apiClient, observer);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, detectionIntervalMiliseconds, createRequestPendingIntent());
    }

    @Override
    protected void onUnsubscribed(GoogleApiClient apiClient) {
        if (apiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(apiClient, createRequestPendingIntent());
            getRequestPendingIntent().cancel();

            mObserver = null;
            mActivityRecognitionPendingIntent = null;
        } else {
            Log.e(TAG, "apiClient not connected");
        }

    }

    /**
     * Returns the current PendingIntent to the caller.
     *
     * @return The PendingIntent used to request activity recognition updates
     */
    public PendingIntent getRequestPendingIntent() {
        return mActivityRecognitionPendingIntent;
    }

    /**
     * Sets the PendingIntent used to make activity recognition update requests
     *
     * @param intent The PendingIntent
     */
    public void setRequestPendingIntent(PendingIntent intent) {
        mActivityRecognitionPendingIntent = intent;
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
        if (null != getRequestPendingIntent()) {

            // Return the existing intent
            return mActivityRecognitionPendingIntent;

            // If no PendingIntent exists
        } else {


            // Create an Intent pointing to the IntentService


    Intent intent = new Intent(ctx, ActivityRecognitionIntentService.class);
//            intent.putExtra(ActivityRecognitionIntentService.REQUEST_RECEIVER_EXTRA, new ResultReceiver(null) {
//                @Override
//                protected void onReceiveResult(int resultCode, Bundle resultData) {
//                    switch (resultCode) {
//                        case ActivityRecognitionIntentService.RESULT_ID_WITH_ACTIVITYRESULT:
//
//                            ActivityRecognitionResult result = resultData.getParcelable(ActivityRecognitionIntentService.RESULT_BUNDLE_ACTIVITYRESULT);
//                            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
//
//                            mObserver.onNext(mostProbableActivity);
//
//                            // Get the confidence percentage for the most probable activity
//                            int confidence = mostProbableActivity.getConfidence();
//
//                            // Get the type of activity
//                            int activityType = mostProbableActivity.getType();
//
//                            Log.d(TAG, getNameFromType(activityType) + " confidence: " + confidence + " isMoving: " + isMoving(activityType));
//                            break;
//                        case ActivityRecognitionIntentService.RESULT_ID_NO_ACTIVITYRESULT:
//                            Log.e(TAG, "Nonfatal: no activity result");
//                            break;
//                        default:
//                            Log.e(TAG, "Unexpected resultCode: " + resultCode);
//                    }
//                }
//            });

            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
    PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);


            setRequestPendingIntent(pendingIntent);
            return pendingIntent;
        }

    }


    /**
     * Determine if an activity means that the user is moving.
     *
     * @param type The type of activity the user is doing (see DetectedActivity constants)
     * @return true if the user seems to be moving from one location to another, otherwise false
     */
    public static boolean isMoving(int type) {
        switch (type) {
            // These types mean that the user is probably not moving
            case DetectedActivity.STILL:
            case DetectedActivity.TILTING:
            case DetectedActivity.UNKNOWN:
                return false;
            default:
                return true;
        }
    }


    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    public static String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }

}
