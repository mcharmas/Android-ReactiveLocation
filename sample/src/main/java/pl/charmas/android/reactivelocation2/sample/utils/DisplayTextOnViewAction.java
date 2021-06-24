package pl.charmas.android.reactivelocation2.sample.utils;

import android.widget.TextView;

import io.reactivex.rxjava3.functions.Consumer;

public class DisplayTextOnViewAction implements Consumer<String> {
    private final TextView target;

    public DisplayTextOnViewAction(TextView target) {
        this.target = target;
    }

    @Override
    public void accept(String s) throws Exception {
        target.setText(s);
    }
}
