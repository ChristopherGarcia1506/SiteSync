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

    private static final String PREFS           = "sitesync_prefs";
    private static final String KEY_SHOW_EXIT   = "show_exit_dialog";
    private static final String KEY_SHOW_ALERTS = "show_alerts";   // snackbars
    private static final String KEY_SHOW_TOASTS = "show_toasts";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_permisons, container, false);

        // Remaining switches in the layout
        Switch swExit   = v.findViewById(R.id.switch_show_exit_dialog);
        Switch swAlerts = v.findViewById(R.id.switch_show_alerts);
        Switch swToasts = v.findViewById(R.id.switch_show_toasts); // keep if you added this

        SharedPreferences sp = requireContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Load
        swExit.setChecked(  sp.getBoolean(KEY_SHOW_EXIT,   true));
        swAlerts.setChecked(sp.getBoolean(KEY_SHOW_ALERTS, true));
        if (swToasts != null) swToasts.setChecked(sp.getBoolean(KEY_SHOW_TOASTS, true));

        // Save
        swExit.setOnCheckedChangeListener((b, on) ->
                sp.edit().putBoolean(KEY_SHOW_EXIT, on).apply());

        swAlerts.setOnCheckedChangeListener((b, on) ->
                sp.edit().putBoolean(KEY_SHOW_ALERTS, on).apply());

        if (swToasts != null) {
            swToasts.setOnCheckedChangeListener((b, on) ->
                    sp.edit().putBoolean(KEY_SHOW_TOASTS, on).apply());
        }

        return v;
    }
}
