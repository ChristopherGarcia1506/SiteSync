
package ca.sitesync.sitesync;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppAlerts {
    private static final String PREFS = "sitesync_prefs";
    private static final String KEY_SHOW_ALERTS = "show_alerts";
    private AppAlerts(){}

    public static boolean enabled(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_SHOW_ALERTS, true);
    }
}
