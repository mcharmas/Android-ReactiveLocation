package pl.charmas.android.reactivelocation.sample;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation.sample.utils.TextObservable;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

import static rx.android.app.AppObservable.bindActivity;

public class PlacesActivity extends ActionBarActivity {

    private TextView currentPlaceView;
    private EditText queryView;
    private ListView placeSuggestionsList;
    private ReactiveLocationProvider reactiveLocationProvider;
    private CompositeSubscription compositeSubscription;

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
    protected void onStart() {
        super.onStart();
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(
                bindActivity(this, reactiveLocationProvider.getCurrentPlace(null))
                        .subscribe(new Action1<PlaceLikelihoodBuffer>() {
                            @Override
                            public void call(PlaceLikelihoodBuffer buffer) {
                                PlaceLikelihood likelihood = buffer.get(0);
                                if (likelihood != null) {
                                    currentPlaceView.setText(likelihood.getPlace().getName());
                                }
                                buffer.release();
                            }
                        })
        );

        Observable<String> queryObservable = TextObservable.create(queryView)
                .debounce(1, TimeUnit.SECONDS)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return !TextUtils.isEmpty(s);
                    }
                });
        Observable<Location> lastKnownLocationObservable = reactiveLocationProvider.getLastKnownLocation();
        Observable<AutocompletePredictionBuffer> suggestionsObservable = Observable
                .combineLatest(queryObservable, lastKnownLocationObservable, new Func2<String, Location, QueryWithCurrentLocation>() {
                    @Override
                    public QueryWithCurrentLocation call(String query, Location currentLocation) {
                        return new QueryWithCurrentLocation(query, currentLocation);
                    }
                }).flatMap(new Func1<QueryWithCurrentLocation, Observable<AutocompletePredictionBuffer>>() {
                    @Override
                    public Observable<AutocompletePredictionBuffer> call(QueryWithCurrentLocation q) {
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

        compositeSubscription.add(bindActivity(this, suggestionsObservable).subscribe(new Action1<AutocompletePredictionBuffer>() {
            @Override
            public void call(AutocompletePredictionBuffer buffer) {
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
        compositeSubscription.unsubscribe();
        compositeSubscription = null;
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
