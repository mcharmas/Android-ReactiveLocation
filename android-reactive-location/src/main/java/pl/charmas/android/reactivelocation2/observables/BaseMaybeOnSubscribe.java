package pl.charmas.android.reactivelocation2.observables;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.disposables.Disposables;

import java.util.Arrays;
import java.util.List;


public abstract class BaseMaybeOnSubscribe<T> implements MaybeOnSubscribe<T> {
    private final Context ctx;
    private final Handler handler;
    private final List<Api<? extends Api.ApiOptions.NotRequiredOptions>> services;

    @SafeVarargs
    protected BaseMaybeOnSubscribe(MaybeContext ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        this.ctx = ctx.getContext();
        this.handler = ctx.getHandler();
        this.services = Arrays.asList(services);
    }

    @Override
    public void subscribe(MaybeEmitter<T> emitter) {
        final GoogleApiClient apiClient = createApiClient(emitter);
        try {
            apiClient.connect();
        } catch (Throwable ex) {
            if (!emitter.isDisposed()) {
                emitter.onError(ex);
            }
        }

        emitter.setDisposable(Disposables.fromAction(() -> {
            onDisposed(apiClient);
            apiClient.disconnect();
        }));
    }


    private GoogleApiClient createApiClient(MaybeEmitter<? super T> emitter) {
        ApiClientConnectionCallbacks apiClientConnectionCallbacks = new ApiClientConnectionCallbacks(emitter);
        GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(ctx);

        for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services) {
            apiClientBuilder.addApi(service);
        }

        apiClientBuilder
                .addConnectionCallbacks(apiClientConnectionCallbacks)
                .addOnConnectionFailedListener(apiClientConnectionCallbacks);

        if (this.handler != null) {
            apiClientBuilder = apiClientBuilder.setHandler(handler);
        }

        GoogleApiClient apiClient = apiClientBuilder.build();
        apiClientConnectionCallbacks.setClient(apiClient);
        return apiClient;
    }

    protected void onDisposed(GoogleApiClient locationClient) {
    }

    protected abstract void onGoogleApiClientReady(GoogleApiClient apiClient, MaybeEmitter<? super T> emitter);

    private class ApiClientConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        final private MaybeEmitter<? super T> emitter;

        private GoogleApiClient apiClient;

        private ApiClientConnectionCallbacks(MaybeEmitter<? super T> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onConnected(Bundle bundle) {
            try {
                onGoogleApiClientReady(apiClient, emitter);
            } catch (Throwable ex) {
                if (!emitter.isDisposed()) {
                    emitter.onError(ex);
                }
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            if (!emitter.isDisposed()) {
                emitter.onError(new GoogleAPIConnectionSuspendedException(cause));
            }
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            if (!emitter.isDisposed()) {
                emitter.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.",
                    connectionResult));
            }
        }

        void setClient(GoogleApiClient client) {
            this.apiClient = client;
        }
    }
}
