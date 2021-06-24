package pl.charmas.android.reactivelocation2.sample.utils;

import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;


class TextViewTextOnSubscribe implements ObservableOnSubscribe<CharSequence> {
    private final TextView view;

    TextViewTextOnSubscribe(TextView view) {
        this.view = view;
    }

    @Override
    public void subscribe(final ObservableEmitter<CharSequence> emitter) throws Exception {

        final TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!emitter.isDisposed()) {
                    emitter.onNext(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        emitter.setDisposable(new MainThreadDisposable() {
            @Override
            protected void onDisposed() {
                view.removeTextChangedListener(watcher);
            }
        });

        view.addTextChangedListener(watcher);

        // Emit initial value.
        emitter.onNext(view.getText());
    }

    abstract class MainThreadDisposable implements Disposable {

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
                                @Override
                                public void run() {
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
