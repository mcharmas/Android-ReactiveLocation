package pl.charmas.android.reactivelocation2

import android.os.Handler

/**
 * Configuration for location provider. Pleas use builder to create an instance.
 */
class ReactiveLocationProviderConfiguration private constructor(builder: Builder) {

    val customCallbackHandler: Handler? = builder.customCallbackHandler
    val isRetryOnConnectionSuspended: Boolean = builder.retryOnConnectionSuspended

    class Builder {
        var customCallbackHandler: Handler? = null
            private set(value) {
                field = value
            }
         var retryOnConnectionSuspended = false
            private set(value) {
                field = value
            }

        /**
         * Allows to set custom handler on which all Google Play Services callbacks are called.
         *
         *
         * Default: null
         *
         * @param customCallbackHandler handler instance
         * @return builder instance
         */
        fun setCustomCallbackHandler(customCallbackHandler: Handler?): Builder {
            this.customCallbackHandler = customCallbackHandler
            return this
        }

        /**
         * Property that allows automatic retries of connection to Google Play Services when it has bean suspended.
         *
         *
         * Default: false
         *
         * @param retryOnConnectionSuspended if should we retry on connection failure
         * @return builder instance
         */
        fun setRetryOnConnectionSuspended(retryOnConnectionSuspended: Boolean): Builder {
            this.retryOnConnectionSuspended = retryOnConnectionSuspended
            return this
        }

        /**
         * Builds configuration instance
         *
         * @return configuration instance
         */
        fun build(): ReactiveLocationProviderConfiguration {
            return ReactiveLocationProviderConfiguration(this)
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}