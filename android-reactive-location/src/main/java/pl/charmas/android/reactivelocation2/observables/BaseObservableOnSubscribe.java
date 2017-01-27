package pl.charmas.android.reactivelocation2.observables;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Arrays;
import java.util.List;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;


public abstract class BaseObservableOnSubscribe<T> implements ObservableOnSubscribe<T> {
    private final Context ctx;
    private final List<Api<? extends Api.ApiOptions.NotRequiredOptions>> services;

    @SafeVarargs
    protected BaseObservableOnSubscribe(Context ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        this.ctx = ctx;
        this.services = Arrays.asList(services);
    }

    @Override
    public void subscribe(ObservableEmitter<T> emitter) throws Exception {
        final GoogleApiClient apiClient = createApiClient(emitter);
        try {
            apiClient.connect();
        } catch (Throwable ex) {
            emitter.onError(ex);
        }

        emitter.setDisposable(Disposables.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (apiClient.isConnected() || apiClient.isConnecting()) {
                    onDisposed(apiClient);
                    apiClient.disconnect();
                }
            }
        }));
    }


    private GoogleApiClient createApiClient(ObservableEmitter<T> emitter) {

        ApiClientConnectionCallbacks apiClientConnectionCallbacks = new ApiClientConnectionCallbacks(emitter);

        GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(ctx);


        for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services) {
            apiClientBuilder.addApi(service);
        }

        apiClientBuilder.addConnectionCallbacks(apiClientConnectionCallbacks);
        apiClientBuilder.addOnConnectionFailedListener(apiClientConnectionCallbacks);

        GoogleApiClient apiClient = apiClientBuilder.build();

        apiClientConnectionCallbacks.setClient(apiClient);

        return apiClient;

    }

    protected void onDisposed(GoogleApiClient locationClient) {
    }

    protected abstract void onGoogleApiClientReady(GoogleApiClient apiClient, ObservableEmitter<T> emitter);

    private class ApiClientConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        final private ObservableEmitter<T> emitter;

        private GoogleApiClient apiClient;

        private ApiClientConnectionCallbacks(ObservableEmitter<T> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onConnected(Bundle bundle) {
            try {
                onGoogleApiClientReady(apiClient, emitter);
            } catch (Throwable ex) {
                emitter.onError(ex);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            emitter.onError(new GoogleAPIConnectionSuspendedException(cause));
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            emitter.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.", connectionResult));
        }

        public void setClient(GoogleApiClient client) {
            this.apiClient = client;
        }
    }

}
