package pl.charmas.android.reactivelocation2.observables;

import com.google.android.gms.common.api.Response;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

public class TaskSingleOnSubscribe<T extends Response> implements SingleOnSubscribe<T> {
    private final Task<T> task;

    public TaskSingleOnSubscribe(Task<T> task) {
        this.task = task;
    }

    @Override
    public void subscribe(final SingleEmitter<T> emitter) {
        task.addOnSuccessListener(new OnSuccessListener<T>() {
            @Override
            public void onSuccess(T t) {
                if (!emitter.isDisposed()) {
                    emitter.onSuccess(t);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                if (!emitter.isDisposed()) {
                    emitter.onError(exception);
                }
            }
        });
    }
}
