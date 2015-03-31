package pl.charmas.android.reactivelocation.sample.utils;

import android.widget.EditText;

import rx.Observable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Func1;

public final class TextObservable {
    private TextObservable() {
    }

    public static Observable<String> create(EditText editText) {
        return WidgetObservable.text(editText, true).map(new Func1<OnTextChangeEvent, String>() {
            @Override
            public String call(OnTextChangeEvent onTextChangeEvent) {
                return onTextChangeEvent.text().toString();
            }
        });
    }
}
