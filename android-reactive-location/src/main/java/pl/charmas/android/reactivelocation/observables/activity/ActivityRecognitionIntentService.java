/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.charmas.android.reactivelocation.observables.activity;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Service that receives ActivityRecognition updates. It receives updates
 * in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {


    private static final String TAG = ActivityRecognitionIntentService.class.getSimpleName();

    public static final String REQUEST_RECEIVER_EXTRA = "REQUEST_RECEIVER_EXTRA";

    public static final String RESULT_BUNDLE_ACTIVITYRESULT = "RESULT_BUNDLE_ACTIVITYRESULT";

    public static final int RESULT_ID_WITH_ACTIVITYRESULT = 200;
    public static final int RESULT_ID_NO_ACTIVITYRESULT = 404;



    public ActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent " + ActivityRecognitionResult.hasResult(intent));
        Log.d(TAG, intent.getExtras().toString());
        // get the passed ResultReceiver
//        ResultReceiver rec = intent.getParcelableExtra(REQUEST_RECEIVER_EXTRA);

        Bundle bundle = new Bundle();

        // If the intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {


            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            DetectedActivity mostProbableActivity = result.getMostProbableActivity();


            if (ActivityUpdatesObservable.getObserver() != null) {
                ActivityUpdatesObservable.getObserver().onNext(mostProbableActivity);
            }

//            bundle.putParcelable(RESULT_BUNDLE_ACTIVITYRESULT, result);
//            rec.send(RESULT_ID_WITH_ACTIVITYRESULT, bundle);
        } else {
//            rec.send(RESULT_ID_NO_ACTIVITYRESULT, bundle);
        }
    }



    /**
     * Tests to see if the activity has changed
     *
     * @param currentType The current activity type
     * @return true if the user's current activity is different from the previous most probable
     * activity; otherwise, false.
     */
//    private boolean activityChanged(int currentType) {
//
//        // Get the previous type, otherwise return the "unknown" type
//        int previousType = mPrefs.getInt(ActivityUtils.KEY_PREVIOUS_ACTIVITY_TYPE,
//                DetectedActivity.UNKNOWN);
//
//        // If the previous type isn't the same as the current type, the activity has changed
//        if (previousType != currentType) {
//            return true;
//
//            // Otherwise, it hasn't.
//        } else {
//            return false;
//        }
//    }


}
