package pl.charmas.android.reactivelocation2.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import io.reactivex.disposables.CompositeDisposable
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import pl.charmas.android.reactivelocation2.sample.utils.UnsubscribeIfPresent

class PlacesResultActivity : BaseActivity() {
    private lateinit var reactiveLocationProvider: ReactiveLocationProvider
    private lateinit var compositeSubscription: CompositeDisposable
    private lateinit var placeNameView: TextView
    private lateinit var placeLocationView: TextView
    private lateinit var placeAddressView: TextView
    private lateinit var placeId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places_result)
        placeNameView = findViewById<View>(R.id.place_name_view) as TextView
        placeLocationView = findViewById<View>(R.id.place_location_view) as TextView
        placeAddressView = findViewById<View>(R.id.place_address_view) as TextView
        reactiveLocationProvider = ReactiveLocationProvider(this)
        placeIdFromIntent
    }

    private val placeIdFromIntent: Unit
        get() {
            val loadedIntent = intent
            placeId = loadedIntent.getStringExtra(EXTRA_PLACE_ID)
                ?: throw IllegalArgumentException("You must start SearchResultsActivity with a non-null place Id using getStartIntent(Context, String)")
        }

    override fun onLocationPermissionGranted() {
        compositeSubscription = CompositeDisposable()
        compositeSubscription.add(
            reactiveLocationProvider.getPlaceById(placeId)
                .subscribe { res ->
                    val place =  res.place
                    placeNameView.text = place.name
                    val text = place.latLng?.latitude.toString() + ", " + place.latLng?.longitude
                    placeLocationView.text = text
                    placeAddressView.text = place.address
                }
        )
    }

    override fun onStop() {
        super.onStop()
        UnsubscribeIfPresent.dispose(compositeSubscription)
    }

    companion object {
        private const val EXTRA_PLACE_ID = "EXTRA_PLACE_ID"
        fun getStartIntent(context: Context?, placeId: String): Intent {
            val startIntent = Intent(context, PlacesResultActivity::class.java)
            startIntent.putExtra(EXTRA_PLACE_ID, placeId)
            return startIntent
        }
    }
}