package pl.charmas.android.reactivelocation2.sample

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import pl.charmas.android.reactivelocation2.sample.ext.toast

abstract class BaseActivity : AppCompatActivity() {

    var disposables = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        val subscribe = RxPermissions(this)
            .request(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .reduce { t1, t2 -> t1 && t2 }
            .subscribe { granted: Boolean ->
                if (granted) {
                    onLocationPermissionGranted()
                } else {
                    toast("Sorry, no demo without permission...")
                }
            }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    protected abstract fun onLocationPermissionGranted()
}