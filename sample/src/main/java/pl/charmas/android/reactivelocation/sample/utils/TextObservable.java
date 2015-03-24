package pl.charmas.android.reactivelocation.sample.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public final class TextObservable {
    private TextObservable() {
    }

    public static Observable<String> create(EditText editText) {
        return Observable.create(new TextWatcherOnSubscribe(editText));
    }

    private static class TextWatcherOnSubscribe implements Observable.OnSubscribe<String> {
        private final EditText editText;

        private TextWatcherOnSubscribe(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void call(final Subscriber<? super String> subscriber) {
            final TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    subscriber.onNext(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            };
            editText.addTextChangedListener(watcher);
            subscriber.onNext(editText.getText().toString());
            subscriber.add(Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    editText.removeTextChangedListener(watcher);
                }
            }));
        }
    }
}
