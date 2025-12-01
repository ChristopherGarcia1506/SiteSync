package ca.sitesync.sitesync;

import static org.junit.Assert.*;

import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Pure-Java unit tests for the preference constants and simple defaults
 * declared in PermissionsFragment. Uses reflection; no Android deps.
 */
public class PermissionsFragmentKeysTest {

    private static String getConst(String name) throws Exception {
        Field f = PermissionsFragment.class.getDeclaredField(name);
        f.setAccessible(true);
        return (String) f.get(null);
    }

    @Test public void prefs_name_is_sitesync_prefs() throws Exception {
        assertEquals("sitesync_prefs", getConst("PREFS"));
    }

    @Test public void key_show_exit_exact() throws Exception {
        assertEquals("show_exit_dialog", getConst("KEY_SHOW_EXIT"));
    }


    @Test public void key_show_alerts_exact() throws Exception {
        assertEquals("show_alerts", getConst("KEY_SHOW_ALERTS"));
    }


    @Test public void key_show_toasts_if_present() throws Exception {
        try {
            assertEquals("show_toasts", getConst("KEY_SHOW_TOASTS"));
        } catch (NoSuchFieldException ex) {

            assertTrue(true);
        }
    }
}



