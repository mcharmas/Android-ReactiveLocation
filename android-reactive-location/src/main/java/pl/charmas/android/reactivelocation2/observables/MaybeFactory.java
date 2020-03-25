package pl.charmas.android.reactivelocation2.observables;

import io.reactivex.Maybe;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeTransformer;
import io.reactivex.functions.BiPredicate;

public class MaybeFactory {
    private final MaybeContext context;

    public MaybeFactory(MaybeContext context) {
        this.context = context;
    }

    public <T> Maybe<T> createMaybe(MaybeOnSubscribe<T> source) {
        return Maybe.create(source).compose(new RetryOnConnectionSuspension<T>(context.isRetryOnConnectionSuspended()));
    }

    private static class RetryOnConnectionSuspension<T> implements MaybeTransformer<T, T> {
        private final boolean shouldRetry;

        RetryOnConnectionSuspension(boolean shouldRetry) {
            this.shouldRetry = shouldRetry;
        }

        @Override
        public MaybeSource<T> apply(Maybe<T> upstream) {
            if (shouldRetry) {
                return upstream.retry(new IsConnectionSuspendedException());
            }
            return upstream;
        }

        private static class IsConnectionSuspendedException implements BiPredicate<Integer, Throwable> {
            @Override
            public boolean test(Integer integer, Throwable throwable) {
                return throwable instanceof GoogleAPIConnectionSuspendedException;
            }
        }
    }
}
