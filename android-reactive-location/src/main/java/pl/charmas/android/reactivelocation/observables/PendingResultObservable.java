package pl.charmas.android.reactivelocation.observables;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class PendingResultObservable<T extends Result> implements Observable.OnSubscribe<T> {
    private final PendingResult<T> result;
    private boolean complete = false;

    public PendingResultObservable(PendingResult<T> result) {
        this.result = result;
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        result.setResultCallback(new ResultCallback<T>() {
            @Override
            public void onResult(T t) {
                subscriber.onNext(t);
                complete = true;
                subscriber.onCompleted();
            }
        });
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (!complete) {
                    result.cancel();
                }
            }
        }));
    }
}
