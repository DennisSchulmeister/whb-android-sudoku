package de.wpvs.sudo_ku;

import android.app.Application;
import android.content.Context;

/**
 * As suggested numerous times in Stack Overflow, the sole purpose of this class is to get an
 * Application Context in places, where it is not natively available (usually outside an
 * activity or fragment). This is used here to get a context for ressource lookup in the
 * database layer, so that we don't have to constantly pass the context as method parameter.
 */
public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
