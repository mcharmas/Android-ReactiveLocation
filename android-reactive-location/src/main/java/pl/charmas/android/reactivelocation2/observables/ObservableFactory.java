package pl.charmas.android.reactivelocation2.observables;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.BiPredicate;

public class ObservableFactory {
    private final ObservableContext context;

    public ObservableFactory(ObservableContext context) {
        this.context = context;
    }

    public <T> Observable<T> createObservable(ObservableOnSubscribe<T> source) {
        return Observable.create(source).compose(new RetryOnConnectionSuspension<T>(context.isRetryOnConnectionSuspended()));
    }

    private static class RetryOnConnectionSuspension<T> implements ObservableTransformer<T, T> {
        private final boolean shouldRetry;

        RetryOnConnectionSuspension(boolean shouldRetry) {
            this.shouldRetry = shouldRetry;
        }

        @Override
        public ObservableSource<T> apply(Observable<T> upstream) {
            if (shouldRetry) {
                return upstream.retry(new IsConnectionSuspendedException());
            }
            return upstream;
        }

        private static class IsConnectionSuspendedException implements BiPredicate<Integer, Throwable> {
            @Override
            public boolean test(Integer integer, Throwable throwable) throws Exception {
                return throwable instanceof GoogleAPIConnectionSuspendedException;
            }
        }
    }
}
