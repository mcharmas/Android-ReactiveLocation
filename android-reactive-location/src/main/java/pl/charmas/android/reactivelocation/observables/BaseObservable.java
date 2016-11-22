package pl.charmas.android.reactivelocation.observables;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;


public abstract class BaseObservable<T> implements Observable.OnSubscribe<T> {
    private WeakReference<Context> ctx;
    private final List<Api<? extends Api.ApiOptions.NotRequiredOptions>> services;

    @SafeVarargs
    protected BaseObservable(Context ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        this.ctx = new WeakReference<>(ctx);
        this.services = Arrays.asList(services);
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {

        final GoogleApiClient apiClient = createApiClient(subscriber);
        try {
            apiClient.connect();
        } catch (Throwable ex) {
            subscriber.onError(ex);
        }

        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (apiClient.isConnected() || apiClient.isConnecting()) {
                    onUnsubscribed(apiClient);
                    apiClient.disconnect();
                }
            }
        }));
    }


    protected GoogleApiClient createApiClient(Subscriber<? super T> subscriber) {

        ApiClientConnectionCallbacks apiClientConnectionCallbacks = new ApiClientConnectionCallbacks(subscriber);

        if (ctx.get() == null)
            return null;
        GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(ctx.get());


        for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services) {
            apiClientBuilder.addApi(service);
        }

        apiClientBuilder.addConnectionCallbacks(apiClientConnectionCallbacks);
        apiClientBuilder.addOnConnectionFailedListener(apiClientConnectionCallbacks);

        GoogleApiClient apiClient = apiClientBuilder.build();

        apiClientConnectionCallbacks.setClient(apiClient);

        return apiClient;

    }

    protected void onUnsubscribed(GoogleApiClient locationClient) {
    }

    protected abstract void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super T> observer);

    private class ApiClientConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        final private Observer<? super T> observer;

        private GoogleApiClient apiClient;

        private ApiClientConnectionCallbacks(Observer<? super T> observer) {
            this.observer = observer;
        }

        @Override
        public void onConnected(Bundle bundle) {
            try {
                onGoogleApiClientReady(apiClient, observer);
            } catch (Throwable ex) {
                observer.onError(ex);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            observer.onError(new GoogleAPIConnectionSuspendedException(cause));
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            observer.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.", connectionResult));
        }

        public void setClient(GoogleApiClient client) {
            this.apiClient = client;
        }
    }

}
