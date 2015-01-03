package pl.charmas.android.reactivelocation.sample.utils;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import rx.functions.Func1;

public class ToMostProbableActivity implements Func1<ActivityRecognitionResult, DetectedActivity> {
    @Override
    public DetectedActivity call(ActivityRecognitionResult activityRecognitionResult) {
        return activityRecognitionResult.getMostProbableActivity();
    }
}
