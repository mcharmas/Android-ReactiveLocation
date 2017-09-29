package pl.charmas.android.reactivelocation.observables;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;


public abstract class BaseObservable<T> implements Observable.OnSubscribe<T> {
    private final android.content.Context ctx;
    private final Handler handler;
    private final List<Api<? extends Api.ApiOptions.NotRequiredOptions>> services;

    @SafeVarargs
    protected BaseObservable(ObservableContext ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        this.ctx = ctx.getContext();
        this.handler = ctx.getHandler();
        this.services = Arrays.asList(services);
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {

        final GoogleApiClient apiClient = createApiClient(subscriber);
        try {
            apiClient.connect();
        } catch (Throwable ex) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(ex);
            }
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


    private GoogleApiClient createApiClient(Subscriber<? super T> subscriber) {
        ApiClientConnectionCallbacks apiClientConnectionCallbacks = new ApiClientConnectionCallbacks(subscriber);
        GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(ctx);

        for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services) {
            apiClientBuilder = apiClientBuilder.addApi(service);
        }

        apiClientBuilder = apiClientBuilder
                .addConnectionCallbacks(apiClientConnectionCallbacks)
                .addOnConnectionFailedListener(apiClientConnectionCallbacks);

        if (this.handler != null) {
            apiClientBuilder = apiClientBuilder.setHandler(handler);
        }

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
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            observer.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.", connectionResult));
        }

        void setClient(GoogleApiClient client) {
            this.apiClient = client;
        }
    }

}
