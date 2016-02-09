package pl.charmas.android.reactivelocation.observables.location;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.api.Status;

public class LocationSettingsActivity extends Activity {

    protected static final String ARG_STATUS = "status";
    protected static final String ARG_ID = "id";

    private static final int REQUEST_CODE_RESOLUTION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            handleIntent();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        Status status = getIntent().getParcelableExtra(ARG_STATUS);

        try {
            status.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException|NullPointerException e) {

            setResolutionResultAndFinish(Activity.RESULT_CANCELED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_RESOLUTION) {
            setResolutionResultAndFinish(resultCode);
        } else {
            setResolutionResultAndFinish(Activity.RESULT_CANCELED);
        }
    }

    private void setResolutionResultAndFinish(int resultCode) {
        LocationSettingsObservable.onResolutionResult(getIntent().getStringExtra(ARG_ID), resultCode);
        finish();
    }
}
