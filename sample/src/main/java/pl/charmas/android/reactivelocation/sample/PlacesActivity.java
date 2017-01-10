package pl.charmas.android.reactivelocation.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation.sample.utils.RxTextView2;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static pl.charmas.android.reactivelocation.sample.utils.UnsubscribeIfPresent.unsubscribe;

public class PlacesActivity extends BaseActivity {

    private TextView currentPlaceView;
    private EditText queryView;
    private ListView placeSuggestionsList;
    private ReactiveLocationProvider reactiveLocationProvider;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        currentPlaceView = (TextView) findViewById(R.id.current_place_view);
        queryView = (EditText) findViewById(R.id.place_query_view);
        placeSuggestionsList = (ListView) findViewById(R.id.place_suggestions_list);
        placeSuggestionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AutocompleteInfo info = (AutocompleteInfo) parent.getAdapter().getItem(position);
                startActivity(PlacesResultActivity.getStartIntent(PlacesActivity.this, info.id));
            }
        });

        reactiveLocationProvider = new ReactiveLocationProvider(this);
    }

    @Override
    protected void onLocationPermissionGranted() {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(
                reactiveLocationProvider.getCurrentPlace(null)
                        .subscribe(new Consumer<PlaceLikelihoodBuffer>() {
                            @Override
                            public void accept(PlaceLikelihoodBuffer buffer) {
                                PlaceLikelihood likelihood = buffer.get(0);
                                if (likelihood != null) {
                                    currentPlaceView.setText(likelihood.getPlace().getName());
                                }
                                buffer.release();
                            }
                        })
        );

        Observable<String> queryObservable = RxTextView2
                .textChanges(queryView)
                .map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) {
                        return charSequence.toString();
                    }
                })
                .debounce(1, TimeUnit.SECONDS)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) {
                        return !TextUtils.isEmpty(s);
                    }
                });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            return;
        }

        Observable<Location> lastKnownLocationObservable = reactiveLocationProvider.getLastKnownLocation();
        Observable<AutocompletePredictionBuffer> suggestionsObservable = Observable
                .combineLatest(queryObservable, lastKnownLocationObservable,
                        new BiFunction<String, Location, QueryWithCurrentLocation>() {
                    @Override
                    public QueryWithCurrentLocation apply(String query, Location currentLocation) {
                        return new QueryWithCurrentLocation(query, currentLocation);
                    }
                }).flatMap(new Function<QueryWithCurrentLocation, Observable<AutocompletePredictionBuffer>>() {
                    @Override
                    public Observable<AutocompletePredictionBuffer> apply(QueryWithCurrentLocation q) {
                        if (q.location == null) return Observable.empty();

                        double latitude = q.location.getLatitude();
                        double longitude = q.location.getLongitude();
                        LatLngBounds bounds = new LatLngBounds(
                                new LatLng(latitude - 0.05, longitude - 0.05),
                                new LatLng(latitude + 0.05, longitude + 0.05)
                        );
                        return reactiveLocationProvider.getPlaceAutocompletePredictions(q.query, bounds, null);
                    }
                });

        compositeDisposable.add(suggestionsObservable.subscribe(new Consumer<AutocompletePredictionBuffer>() {
            @Override
            public void accept(AutocompletePredictionBuffer buffer) {
                List<AutocompleteInfo> infos = new ArrayList<>();
                for (AutocompletePrediction prediction : buffer) {
                    infos.add(new AutocompleteInfo(prediction.getFullText(null).toString(), prediction.getPlaceId()));
                }
                buffer.release();
                placeSuggestionsList.setAdapter(new ArrayAdapter<>(PlacesActivity.this, android.R.layout.simple_list_item_1, infos));
            }
        }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unsubscribe(compositeDisposable);
    }

    private static class QueryWithCurrentLocation {
        public final String query;
        public final Location location;

        private QueryWithCurrentLocation(String query, Location location) {
            this.query = query;
            this.location = location;
        }
    }

    private static class AutocompleteInfo {
        private final String description;
        private final String id;

        private AutocompleteInfo(String description, String id) {
            this.description = description;
            this.id = id;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
