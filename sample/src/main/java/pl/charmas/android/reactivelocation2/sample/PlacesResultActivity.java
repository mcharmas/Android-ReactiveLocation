package pl.charmas.android.reactivelocation2.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

import static pl.charmas.android.reactivelocation2.sample.utils.UnsubscribeIfPresent.dispose;

public class PlacesResultActivity extends BaseActivity {

    private static final String EXTRA_PLACE_ID = "EXTRA_PLACE_ID";

    private ReactiveLocationProvider reactiveLocationProvider;
    private CompositeDisposable compositeSubscription;
    private TextView placeNameView;
    private TextView placeLocationView;
    private TextView placeAddressView;
    private String placeId;

    public static Intent getStartIntent(Context context, @NonNull String placeId) {
        Intent startIntent = new Intent(context, PlacesResultActivity.class);
        startIntent.putExtra(EXTRA_PLACE_ID, placeId);

        return startIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_result);

        placeNameView = (TextView) findViewById(R.id.place_name_view);
        placeLocationView = (TextView) findViewById(R.id.place_location_view);
        placeAddressView = (TextView) findViewById(R.id.place_address_view);

        reactiveLocationProvider = new ReactiveLocationProvider(this);

        getPlaceIdFromIntent();
    }

    private void getPlaceIdFromIntent() {
        Intent loadedIntent = getIntent();
        placeId = loadedIntent.getStringExtra(EXTRA_PLACE_ID);

        if (placeId == null) {
            throw new IllegalStateException("You must start SearchResultsActivity with a non-null place Id using getStartIntent(Context, String)");
        }
    }

    @Override
    protected void onLocationPermissionGranted() {
        compositeSubscription = new CompositeDisposable();
        compositeSubscription.add(reactiveLocationProvider.getPlaceById(placeId)
                .subscribe(new Consumer<PlaceBuffer>() {
                    @Override
                    public void accept(PlaceBuffer buffer) {
                        Place place = buffer.get(0);
                        if (place != null) {
                            placeNameView.setText(place.getName());
                            placeLocationView.setText(place.getLatLng().latitude + ", " + place.getLatLng().longitude);
                            placeAddressView.setText(place.getAddress());
                        }
                        buffer.release();
                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        dispose(compositeSubscription);
    }
}
