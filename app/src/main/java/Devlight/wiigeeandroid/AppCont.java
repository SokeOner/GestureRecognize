package Devlight.wiigeeandroid;

import android.app.Application;
import android.content.Context;

import com.stanko.tools.SharedPrefsHelper;

/**
 * Created by Victor Pavluchinskyy email: sokeOner9@gmail.com on 8/21/15.
 */
public class AppCont extends Application {
    private static Context appContext;

    public static Context getAppContext() {
        return appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        SharedPrefsHelper.init(this);
    }
}
