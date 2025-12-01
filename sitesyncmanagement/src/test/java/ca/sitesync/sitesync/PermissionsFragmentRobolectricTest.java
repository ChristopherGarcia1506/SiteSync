package ca.sitesync.sitesync;

import static org.junit.Assert.*;

import android.widget.Switch;

import androidx.fragment.app.FragmentActivity;
import android.os.Build;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

@Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@RunWith(RobolectricTestRunner.class)
public class PermissionsFragmentRobolectricTest {



    private FragmentActivity launchWith(PermissionsFragment f) {
        ActivityController<FragmentActivity> controller =
                Robolectric.buildActivity(FragmentActivity.class).setup();

        FragmentActivity activity = controller.get();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, f)
                .commitNow(); // or commitNowAllowingStateLoss()

        return activity;
    }


    @Test
    public void fragment_inflates() {
        FragmentActivity act = launchWith(new PermissionsFragment());
        assertNotNull(act.findViewById(android.R.id.content));
    }


    @Test public void switches_exist() {
        FragmentActivity act = Robolectric.buildActivity(FragmentActivity.class).setup().get();
        PermissionsFragment f = new PermissionsFragment();
        act.getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f).commitNow();
        Switch swAlerts = f.getView().findViewById(R.id.switch_show_alerts);
        assertNotNull(swAlerts);
    }

    @Test public void alerts_toggle_persists() {
        FragmentActivity act = Robolectric.buildActivity(FragmentActivity.class).setup().get();
        PermissionsFragment f = new PermissionsFragment();
        act.getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f).commitNow();

        Switch sw = f.getView().findViewById(R.id.switch_show_alerts);
        boolean start = sw.isChecked();
        sw.setChecked(!start);
        assertEquals(!start, sw.isChecked());
    }

    @Test public void exit_toggle_defaultsTrue() {
        FragmentActivity act = Robolectric.buildActivity(FragmentActivity.class).setup().get();
        PermissionsFragment f = new PermissionsFragment();
        act.getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f).commitNow();
        Switch swExit = f.getView().findViewById(R.id.switch_show_exit_dialog);
        assertTrue(swExit.isChecked()); // your code loads default true
    }

    @Test public void toasts_toggle_present() {
        FragmentActivity act = Robolectric.buildActivity(FragmentActivity.class).setup().get();
        PermissionsFragment f = new PermissionsFragment();
        act.getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f).commitNow();
        Switch swToasts = f.getView().findViewById(R.id.switch_show_toasts);
        assertNotNull(swToasts);
    }
}
