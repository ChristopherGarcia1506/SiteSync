package ca.sitesync.sitesync;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class PrefsLocalTest {

    private static final String PREFS = "sitesync_prefs";
    private static final String KEY_SHOW_EXIT   = "show_exit_dialog";
    private static final String KEY_ALLOW_EDIT  = "allow_edit_job";
    private static final String KEY_SHOW_ALERTS = "show_alerts";

    private SharedPreferences sp;

    @Before public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().clear().commit();
    }


    @Test public void default_showExit_isTrue() {
        assertTrue(sp.getBoolean(KEY_SHOW_EXIT, true));
    }


    @Test public void persist_allowEdit_false() {
        sp.edit().putBoolean(KEY_ALLOW_EDIT, false).commit();
        assertFalse(sp.getBoolean(KEY_ALLOW_EDIT, true));
    }


    @Test public void default_showAlerts_isTrue() {
        assertTrue(sp.getBoolean(KEY_SHOW_ALERTS, true));
    }


    @Test public void toggle_showAlerts_off_persists() {
        sp.edit().putBoolean(KEY_SHOW_ALERTS, false).commit();
        assertFalse(sp.getBoolean(KEY_SHOW_ALERTS, true));
    }
}
