package pl.charmas.android.reactivelocation2;

import android.os.Handler;

import androidx.annotation.Nullable;

/**
 * Configuration for location provider. Pleas use builder to create an instance.
 */
public class ReactiveLocationProviderConfiguration {
    private final Handler customCallbackHandler;
    private final boolean retryOnConnectionSuspended;

    private ReactiveLocationProviderConfiguration(Builder builder) {
        this.customCallbackHandler = builder.customCallbackHandler;
        this.retryOnConnectionSuspended = builder.retryOnConnectionSuspended;
    }

    public Handler getCustomCallbackHandler() {
        return customCallbackHandler;
    }

    public boolean isRetryOnConnectionSuspended() {
        return retryOnConnectionSuspended;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Handler customCallbackHandler = null;
        private boolean retryOnConnectionSuspended = false;

        /**
         * Allows to set custom handler on which all Google Play Services callbacks are called.
         * <p>
         * Default: null
         *
         * @param customCallbackHandler handler instance
         * @return builder instance
         */
        public Builder setCustomCallbackHandler(@Nullable Handler customCallbackHandler) {
            this.customCallbackHandler = customCallbackHandler;
            return this;
        }

        /**
         * Property that allows automatic retries of connection to Google Play Services when it has bean suspended.
         * <p>
         * Default: false
         *
         * @param retryOnConnectionSuspended if should we retry on connection failure
         * @return builder instance
         */
        public Builder setRetryOnConnectionSuspended(boolean retryOnConnectionSuspended) {
            this.retryOnConnectionSuspended = retryOnConnectionSuspended;
            return this;
        }

        /**
         * Builds configuration instance
         *
         * @return configuration instance
         */
        public ReactiveLocationProviderConfiguration build() {
            return new ReactiveLocationProviderConfiguration(this);
        }
    }
}
