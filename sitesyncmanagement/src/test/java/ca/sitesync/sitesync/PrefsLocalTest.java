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

    @Test
    public void toggle_showAlerts_persistsAcrossInstances() {
        Context ctx = ApplicationProvider.getApplicationContext();

        // First session turn OFF alerts
        var sp1 = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp1.edit().putBoolean(KEY_SHOW_ALERTS, false).commit();

        // Second re-acquire prefs and make sure it stayed OFF
        var sp2 = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        assertFalse(sp2.getBoolean(KEY_SHOW_ALERTS, true));
    }


    public void toggle_showExit_off_persistsAcrossInstances() {
        Context ctx = ApplicationProvider.getApplicationContext();

        // First session turn OFF exit dialog
        SharedPreferences sp1 = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp1.edit().putBoolean(KEY_SHOW_EXIT, false).commit();

        // Second session re-acquire and verify it stayed off
        SharedPreferences sp2 = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        assertFalse(sp2.getBoolean(KEY_SHOW_EXIT, true));
    }


    @Test
    public void clear_prefs_resets_to_defaults() {
        // set non-defaults
        sp.edit()
                .putBoolean(KEY_SHOW_EXIT,   false)
                .putBoolean(KEY_ALLOW_EDIT,  false)
                .putBoolean(KEY_SHOW_ALERTS, false)
                .commit();

        // clear then verify defaults are read as true
        sp.edit().clear().commit();

        assertTrue(sp.getBoolean(KEY_SHOW_EXIT,   true));
        assertTrue(sp.getBoolean(KEY_ALLOW_EDIT,  true));
        assertTrue(sp.getBoolean(KEY_SHOW_ALERTS, true));
    }










}
