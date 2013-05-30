
package com.egloos.realmove.android.fp;

import android.app.Application;

public class App extends Application {

    private static Application instance = null;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    public static Application getInstacne() {
        return instance;
    }

}
