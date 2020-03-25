package pl.charmas.android.reactivelocation2.observables;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;

public class PendingResultObservableOnSubscribe<T extends Result> implements ObservableOnSubscribe<T> {
    private final PendingResult<T> result;
    private boolean complete = false;

    public PendingResultObservableOnSubscribe(PendingResult<T> result) {
        this.result = result;
    }

    @Override
    public void subscribe(final ObservableEmitter<T> emitter) {
        result.setResultCallback(t -> {
            if (!emitter.isDisposed()) {
                emitter.onNext(t);
                emitter.onComplete();
            }
            complete = true;
        });

        emitter.setDisposable(Disposables.fromAction(() -> {
            if (!complete) {
                result.cancel();
            }
        }));
    }
}
