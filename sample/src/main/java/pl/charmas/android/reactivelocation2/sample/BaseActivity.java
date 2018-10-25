package pl.charmas.android.reactivelocation2.sample;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
        new RxPermissions(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (granted) {
                            onLocationPermissionGranted();
                        } else {
                            Toast.makeText(BaseActivity.this, "Sorry, no demo without permission...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    protected abstract void onLocationPermissionGranted();
}
