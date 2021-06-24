package pl.charmas.android.reactivelocation2.sample.utils;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import io.reactivex.rxjava3.functions.Function;

public class ToMostProbableActivity implements Function<ActivityRecognitionResult, DetectedActivity> {
    @Override
    public DetectedActivity apply(ActivityRecognitionResult activityRecognitionResult) {
        return activityRecognitionResult.getMostProbableActivity();
    }
}
