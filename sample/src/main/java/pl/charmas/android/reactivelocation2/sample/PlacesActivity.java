package pl.charmas.android.reactivelocation2.sample;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation2.sample.utils.RxTextView;

import static pl.charmas.android.reactivelocation2.sample.utils.UnsubscribeIfPresent.dispose;

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
                        .subscribe(new Consumer<PlaceLikelihoodBufferResponse>() {
                            @Override
                            public void accept(PlaceLikelihoodBufferResponse response) throws Exception {
                                PlaceLikelihood likelihood = response.get(0);
                                if (likelihood != null) {
                                    currentPlaceView.setText(likelihood.getPlace().getName());
                                }
                                response.release();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e("PlacesActivity", "Error in observable", throwable);
                            }
                        })
        );

        Observable<String> queryObservable = RxTextView
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
        Single<Location> lastKnownLocationObservable = reactiveLocationProvider.getLastKnownLocation();
        Observable<AutocompletePredictionBufferResponse> suggestionsObservable = Observable
                .combineLatest(queryObservable, lastKnownLocationObservable.toObservable(),
                        new BiFunction<String, Location, QueryWithCurrentLocation>() {
                            @Override
                            public QueryWithCurrentLocation apply(String query, Location currentLocation) {
                                return new QueryWithCurrentLocation(query, currentLocation);
                            }
                        })
                .flatMapMaybe(new Function<QueryWithCurrentLocation, MaybeSource<AutocompletePredictionBufferResponse>>() {
                    @Override
                    public MaybeSource<AutocompletePredictionBufferResponse> apply(QueryWithCurrentLocation q) {
                        if (q.location == null) return Maybe.empty();

                        double latitude = q.location.getLatitude();
                        double longitude = q.location.getLongitude();
                        LatLngBounds bounds = new LatLngBounds(
                                new LatLng(latitude - 0.05, longitude - 0.05),
                                new LatLng(latitude + 0.05, longitude + 0.05)
                        );
                        return reactiveLocationProvider.getPlaceAutocompletePredictions(q.query, bounds, null).toMaybe();
                    }
                });

        compositeDisposable.add(suggestionsObservable.subscribe(new Consumer<AutocompletePredictionBufferResponse>() {
            @Override
            public void accept(AutocompletePredictionBufferResponse buffer) {
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
        dispose(compositeDisposable);
    }

    private static class QueryWithCurrentLocation {
        final String query;
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
