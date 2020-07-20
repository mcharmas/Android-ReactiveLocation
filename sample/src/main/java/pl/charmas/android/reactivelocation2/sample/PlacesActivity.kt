package pl.charmas.android.reactivelocation2.sample

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import pl.charmas.android.reactivelocation2.sample.utils.RxTextView
import pl.charmas.android.reactivelocation2.sample.utils.UnsubscribeIfPresent
import java.util.concurrent.TimeUnit

class PlacesActivity : BaseActivity() {

    private lateinit var currentPlaceView: TextView
    private lateinit var queryView: EditText
    private lateinit var placeSuggestionsList: ListView
    private lateinit var reactiveLocationProvider: ReactiveLocationProvider
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)
        Places.initialize(this.applicationContext, getString(R.string.API_KEY))
        currentPlaceView = findViewById(R.id.current_place_view)
        queryView = findViewById(R.id.place_query_view)
        placeSuggestionsList = findViewById(R.id.place_suggestions_list)
        placeSuggestionsList.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val info =
                    parent.adapter.getItem(position) as AutocompleteInfo
                val placeId = info.id
                if (placeId != null) {
                    startActivity(PlacesResultActivity.getStartIntent(this@PlacesActivity, placeId))
                }
            }
        reactiveLocationProvider = ReactiveLocationProvider(this)
    }

    @SuppressLint("MissingPermission")
    override fun onLocationPermissionGranted() {
        compositeDisposable.add(
            reactiveLocationProvider.getCurrentPlace(
                FindCurrentPlaceRequest.builder(listOf(com.google.android.libraries.places.api.model.Place.Field.ID))
                    .build()
            )
                .subscribe({ response ->
                    val buffer = response.placeLikelihoods
                    val likelihood = buffer.firstOrNull()
                    if (likelihood != null) {
                        currentPlaceView.text = likelihood.place.name
                    }
                }) { throwable ->
                    Log.e("PlacesActivity", "Error in observable", throwable)
                }
        )
        val queryObservable = RxTextView
            .textChanges(queryView)
            .map { charSequence -> charSequence.toString() }
            .debounce(1, TimeUnit.SECONDS)
            .filter { s -> s.isNotEmpty() }

        compositeDisposable.add(
            Observable.combineLatest(
                queryObservable,
                reactiveLocationProvider.lastKnownLocation,
                BiFunction<String, Location, QueryWithCurrentLocation> { query, currentLocation ->
                    QueryWithCurrentLocation(query, currentLocation)
                }
            )
                .flatMapMaybe { q ->
                    val latitude = q.location.latitude
                    val longitude = q.location.longitude
                    val bounds = LatLngBounds(
                        LatLng(latitude - 50.05, longitude - 50.05),
                        LatLng(latitude + 50.05, longitude + 50.05)
                    )
                    reactiveLocationProvider.getPlaceAutocompletePredictions(
                        FindAutocompletePredictionsRequest.builder()
                            .setQuery(q.query)
                            .setLocationRestriction(RectangularBounds.newInstance(bounds))
                            .build()
                    )
                }
                .doOnError { Log.e(TAG, "onLocationPermissionGranted (line 80): ", it) }
                .retry()
                .subscribe { buffer ->
                    val infos = mutableListOf<AutocompleteInfo>()
                    for (prediction in buffer) {
                        infos.add(

                            AutocompleteInfo(
                                prediction.getFullText(null).toString(),
                                prediction.placeId
                            )
                        )
                    }
                    placeSuggestionsList.adapter = ArrayAdapter(
                        this@PlacesActivity,
                        android.R.layout.simple_list_item_1,
                        infos
                    )
                }
        )
    }

    override fun onStop() {
        super.onStop()
        UnsubscribeIfPresent.dispose(compositeDisposable)
    }

    data class QueryWithCurrentLocation(val query: String, val location: Location)

    data class AutocompleteInfo(
        val description: String,
        val id: String?
    ) {
        override fun toString(): String {
            return description
        }
    }

    companion object {
        const val TAG: String = "PlacesActivity"
    }
}