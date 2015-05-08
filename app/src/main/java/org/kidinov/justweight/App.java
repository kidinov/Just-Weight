package org.kidinov.justweight;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.orm.SugarApp;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by akid on 18/04/15.
 */
public class App extends SugarApp {
    private static Tracker tracker;

    public static final boolean IS_TESTING = false;

    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("Roboto-Regular.ttf").build());
    }

    public synchronized Tracker getTracker() {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        if (tracker == null) {
            tracker = analytics.newTracker("UA-43322132-6");
        }
        return tracker;
    }
}
