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

    private static final String PREFS = "sitesync_prefs";
    private static final String KEY_SHOW_EXIT = "show_exit_dialog";   // TOP switch
    private static final String KEY_ALLOW_EDIT = "allow_edit_job";    // MIDDLE switch (your placeholder text)
    private static final String KEY_SHOW_ALERTS = "show_alerts";      // BOTTOM switch

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_permisons, container, false);

        // match your XML ids
        Switch swExit   = v.findViewById(R.id.switch_show_exit_dialog);
        Switch swEdit   = v.findViewById(R.id.switch_allow_job_edit);
        Switch swAlerts = v.findViewById(R.id.switch_show_alerts);

        SharedPreferences sp = requireContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // load saved values
        swExit.setChecked(sp.getBoolean(KEY_SHOW_EXIT, true));
        swEdit.setChecked(sp.getBoolean(KEY_ALLOW_EDIT, true));
        swAlerts.setChecked(sp.getBoolean(KEY_SHOW_ALERTS, true));

        // save on toggle
        swExit.setOnCheckedChangeListener((btn, isChecked) ->
                sp.edit().putBoolean(KEY_SHOW_EXIT, isChecked).apply());

        swEdit.setOnCheckedChangeListener((btn, isChecked) ->
                sp.edit().putBoolean(KEY_ALLOW_EDIT, isChecked).apply());

        swAlerts.setOnCheckedChangeListener((btn, isChecked) ->
                sp.edit().putBoolean(KEY_SHOW_ALERTS, isChecked).apply());

        return v;
    }
}
