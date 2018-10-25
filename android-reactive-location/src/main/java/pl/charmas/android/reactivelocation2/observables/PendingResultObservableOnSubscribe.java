package pl.charmas.android.reactivelocation2.observables;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;

public class PendingResultObservableOnSubscribe<T extends Result> implements ObservableOnSubscribe<T> {
    private final PendingResult<T> result;
    private boolean complete = false;

    public PendingResultObservableOnSubscribe(PendingResult<T> result) {
        this.result = result;
    }

    @Override
    public void subscribe(final ObservableEmitter<T> emitter) throws Exception {
        result.setResultCallback(new ResultCallback<T>() {
            @Override
            public void onResult(@NonNull T t) {
                if (!emitter.isDisposed()) {
                    emitter.onNext(t);
                    emitter.onComplete();
                }
                complete = true;
            }
        });

        emitter.setDisposable(Disposables.fromAction(new Action() {
            @Override
            public void run() {
                if (!complete) {
                    result.cancel();
                }
            }
        }));
    }
}
