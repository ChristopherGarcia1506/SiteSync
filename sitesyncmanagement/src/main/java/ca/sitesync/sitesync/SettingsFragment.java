/*
Anthony Mancia (N01643670) OCB
Chris Garcia (N01371506) 0CA
Ngoc Le (N01643011) 0CA
Tyler Meira (N01432291) 0CA
*/
package ca.sitesync.sitesync;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "AppSettings";
    private static final String ROTATION_LOCK_KEY = "rotation_locked";
    private static final String DARK_MODE_KEY = "dark_mode";
    private static final String DARK_MODE_SYSTEM = "system";
    private static final String DARK_MODE_DARK = "dark";

    private boolean isRotationLocked = false;
    private String currentDarkMode = DARK_MODE_SYSTEM;
    private SharedPreferences sharedPreferences;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize shared preferences and load saved state
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        isRotationLocked = sharedPreferences.getBoolean(ROTATION_LOCK_KEY, false);
        currentDarkMode = sharedPreferences.getString(DARK_MODE_KEY, DARK_MODE_SYSTEM);

        // Apply rotation lock immediately if it was enabled
        if (isRotationLocked && getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ListView listView = view.findViewById(R.id.listView);

        List<String> items = new ArrayList<>();
        items.add(getString(R.string.rotation_lock) + (isRotationLocked ? " (Enabled)" : ""));
        items.add(getString(R.string.dark_mode) + (currentDarkMode.equals(DARK_MODE_DARK) ? "On" : "Off"));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        //---ListView Click Listener---
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Rotation Lock item
                        toggleRotationLock();
                        refreshListView(listView);
                        break;
                    case 1: // Dark Mode item
                        toggleDarkMode();
                        refreshListView(listView);
                        break;
                }
            }
        });

        return view;
    }

    private void toggleRotationLock() {
        if (getActivity() != null) {
            if (isRotationLocked) {
                // Unlock rotation - allow both portrait and landscape
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                isRotationLocked = false;
                Toast.makeText(getContext(), R.string.rotation_unlocked, Toast.LENGTH_SHORT).show();
            } else {
                // Lock rotation to portrait mode
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                isRotationLocked = true;
                Toast.makeText(getContext(), R.string.rotation_locked_to_portrait, Toast.LENGTH_SHORT).show();
            }

            // Save the state
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(ROTATION_LOCK_KEY, isRotationLocked);
            editor.apply();
        }
    }

    private void toggleDarkMode() {
        if (currentDarkMode.equals(DARK_MODE_SYSTEM)) {
            currentDarkMode = DARK_MODE_DARK;
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(getContext(), R.string.dark_mode_enabled, Toast.LENGTH_SHORT).show();
        } else {
            currentDarkMode = DARK_MODE_SYSTEM;
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            Toast.makeText(getContext(), R.string.light_mode_enabled, Toast.LENGTH_SHORT).show();
        }

        // Save the preference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DARK_MODE_KEY, currentDarkMode);
        editor.apply();

        // Restart activity to apply theme changes immediately
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }

    private void refreshListView(ListView listView) {
        List<String> items = new ArrayList<>();
        items.add(getString(R.string.rotation_lock) + (isRotationLocked ? " (Enabled)" : ""));
        items.add(getString(R.string.dark_mode) + (currentDarkMode.equals(DARK_MODE_DARK) ? "On" : "Off"));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }
}
