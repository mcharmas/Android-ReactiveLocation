package pl.charmas.android.reactivelocation2.sample.utils;

import com.google.android.gms.location.DetectedActivity;

import io.reactivex.rxjava3.functions.Function;

public class DetectedActivityToString implements Function<DetectedActivity, String> {
    @Override
    public String apply(DetectedActivity detectedActivity) {
        return getNameFromType(detectedActivity.getType()) + " with confidence " + detectedActivity.getConfidence();
    }

    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.RUNNING:
                return "running";
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
