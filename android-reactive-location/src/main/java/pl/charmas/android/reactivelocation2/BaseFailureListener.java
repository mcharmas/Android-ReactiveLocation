package pl.charmas.android.reactivelocation2;

import com.google.android.gms.tasks.OnFailureListener;

import androidx.annotation.NonNull;
import io.reactivex.ObservableEmitter;

public class BaseFailureListener<T> implements OnFailureListener {

    private final ObservableEmitter<? super T> emitter;

    public BaseFailureListener(ObservableEmitter<? super T> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
        if (emitter.isDisposed()) return;
        emitter.onError(exception);
        emitter.onComplete();
    }
}
