package pl.charmas.android.reactivelocation2.observables;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.functions.BiPredicate;

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
