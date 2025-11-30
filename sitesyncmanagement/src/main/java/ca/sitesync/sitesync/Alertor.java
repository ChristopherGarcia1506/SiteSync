package ca.sitesync.sitesync;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.StringRes;

public final class Alertor {
    private static final String PREFS = "sitesync_prefs";
    private static final String KEY_SHOW_TOASTS = "show_toasts";

    private Alertor() {}

    private static boolean toastsOn(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_SHOW_TOASTS, true);
    }

    public static void toast(Context ctx, @StringRes int msg) {
        if (!toastsOn(ctx)) return;
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context ctx, String msg) {
        if (!toastsOn(ctx)) return;
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }
}
