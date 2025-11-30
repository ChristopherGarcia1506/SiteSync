package ca.sitesync.sitesync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PermissionsFragment extends Fragment {

    // SharedPreferences file + keys
    private static final String PREFS            = "sitesync_prefs";
    private static final String KEY_SHOW_EXIT    = "show_exit_dialog";
    private static final String KEY_ALLOW_EDIT   = "allow_edit_job";
    private static final String KEY_SHOW_ALERTS  = "show_alerts";   // snackbars/alerts
    private static final String KEY_SHOW_TOASTS  = "show_toasts";   // <-- NEW: toasts toggle

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // keep this name exactly as your XML file name
        View v = inflater.inflate(R.layout.fragment_permisons, container, false);

        // bind all switches that exist in your XML (ids must match)
        Switch swExit    = v.findViewById(R.id.switch_show_exit_dialog);
        Switch swEdit    = v.findViewById(R.id.switch_allow_job_edit);     // ok if null
        Switch swAlerts  = v.findViewById(R.id.switch_show_alerts);
        Switch swToasts  = v.findViewById(R.id.switch_show_toasts);        // <-- NEW

        SharedPreferences sp = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // load saved values
        if (swExit   != null) swExit.setChecked(  sp.getBoolean(KEY_SHOW_EXIT,   true));
        if (swEdit   != null) swEdit.setChecked(  sp.getBoolean(KEY_ALLOW_EDIT,  true));
        if (swAlerts != null) swAlerts.setChecked(sp.getBoolean(KEY_SHOW_ALERTS, true));
        if (swToasts != null) swToasts.setChecked(sp.getBoolean(KEY_SHOW_TOASTS, true));

        // save on toggle
        if (swExit != null) {
            swExit.setOnCheckedChangeListener((btn, on) ->
                    sp.edit().putBoolean(KEY_SHOW_EXIT, on).apply());
        }

        if (swEdit != null) {
            swEdit.setOnCheckedChangeListener((btn, on) ->
                    sp.edit().putBoolean(KEY_ALLOW_EDIT, on).apply());
        }

        if (swAlerts != null) {
            swAlerts.setOnCheckedChangeListener((btn, on) ->
                    sp.edit().putBoolean(KEY_SHOW_ALERTS, on).apply());
        }

        if (swToasts != null) {
            swToasts.setOnCheckedChangeListener((btn, on) ->
                    sp.edit().putBoolean(KEY_SHOW_TOASTS, on).apply());
        }

        return v;
    }
}
