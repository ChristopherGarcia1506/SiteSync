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
    private static final String KEY_SHOW_EXIT = "show_exit_dialog";
    private static final String KEY_ALLOW_EDIT = "allow_edit_job";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_permisons, container, false);

        Switch swExit = v.findViewById(R.id.switch_show_exit_dialog);
        Switch swEdit = v.findViewById(R.id.switch_allow_job_edit);

        SharedPreferences sp = requireContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // load saved values
        boolean showExit = sp.getBoolean(KEY_SHOW_EXIT, true);
        boolean allowEdit = sp.getBoolean(KEY_ALLOW_EDIT, true);

        swExit.setChecked(showExit);
        swEdit.setChecked(allowEdit);

        swExit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean(KEY_SHOW_EXIT, isChecked).apply();
        });

        swEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean(KEY_ALLOW_EDIT, isChecked).apply();
        });

        return v;
    }
}
