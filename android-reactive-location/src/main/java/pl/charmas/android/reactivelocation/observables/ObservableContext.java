package pl.charmas.android.reactivelocation.observables;

import android.content.Context;
import android.os.Handler;

public class ObservableContext {
    private final Context context;
    private final Handler handler;

    public ObservableContext(android.content.Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public Context getContext() {
        return context;
    }

    Handler getHandler() {
        return handler;
    }
}
