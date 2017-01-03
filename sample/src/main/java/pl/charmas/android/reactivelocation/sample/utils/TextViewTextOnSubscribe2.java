package pl.charmas.android.reactivelocation.sample.utils;

import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;



/**
 * {@link com.jakewharton.rxbinding.widget.TextViewTextOnSubscribe} compatible with rxjava2
 */

public class TextViewTextOnSubscribe2 implements ObservableOnSubscribe<CharSequence> {
    final TextView view;

    TextViewTextOnSubscribe2(TextView view) {
        this.view = view;
    }


    @Override
    public void subscribe(final ObservableEmitter<CharSequence> emitter) throws Exception {

        final TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!emitter.isDisposed()) {
                    emitter.onNext(s);
                }
            }

            @Override public void afterTextChanged(Editable s) {
            }
        };

        emitter.setDisposable(new MainThreadDisposable() {
            @Override protected void onDisposed() {
                view.removeTextChangedListener(watcher);
            }
        });

        view.addTextChangedListener(watcher);

        // Emit initial value.
        emitter.onNext(view.getText());
    }

    public abstract class MainThreadDisposable implements Disposable {

        private final AtomicBoolean dispose = new AtomicBoolean();

        @Override
        public void dispose() {
            if (dispose.compareAndSet(false, true)) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    onDisposed();
                } else {
                    AndroidSchedulers.mainThread()
                            .createWorker()
                            .schedule(new Runnable() {
                        @Override public void run() {
                            onDisposed();
                        }
                    });
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return dispose.get();
        }

        abstract void onDisposed();
    }
}
