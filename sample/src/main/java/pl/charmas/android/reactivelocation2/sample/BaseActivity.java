package pl.charmas.android.reactivelocation2.sample;

import android.Manifest;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.disposables.Disposable;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
        Disposable subscribe = new RxPermissions(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        onLocationPermissionGranted();
                    } else {
                        Toast.makeText(BaseActivity.this, "Sorry, no demo without permission...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected abstract void onLocationPermissionGranted();
}
