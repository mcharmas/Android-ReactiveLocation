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
import android.support.v4.content.LocalBroadcastManager;

/**
* Service that receives ActivityRecognition updates. It receives updates
* in the background, even if the main Activity is not visible.
*/
public class ActivityRecognitionIntentService extends IntentService {


    private static final String TAG = ActivityRecognitionIntentService.class.getSimpleName();


    public ActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Intent i = new Intent(ActivityUpdatesObservable.ACTION_ACTIVITY_DETECTED);
        i.fillIn(intent, Intent.FILL_IN_DATA);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);

    }



}
