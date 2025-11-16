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

    private String mParam1;
    private String mParam2;
    private boolean isRotationLocked = false;
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
        if (getArguments() != null) {

        }

        // Initialize shared preferences and load saved state
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        isRotationLocked = sharedPreferences.getBoolean(ROTATION_LOCK_KEY, false);

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
        items.add("Rotation Lock" + (isRotationLocked ? " (Enabled)" : ""));
        items.add("Profile Picture");
        items.add("Manage Accounts");
        items.add("Change password");
        items.add("LogOut");
        //About & perms moved to overflow menu in top right of screen


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);


        return view;
    }

    private void toggleRotationLock() {
        if (getActivity() != null) {
            if (isRotationLocked) {
                // Unlock rotation - allow both portrait and landscape
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                isRotationLocked = false;
                Toast.makeText(getContext(), "Rotation unlocked - Auto-rotate enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Lock rotation to portrait mode
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                isRotationLocked = true;
                Toast.makeText(getContext(), "Rotation locked to portrait mode", Toast.LENGTH_SHORT).show();
            }

            // Save the state
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(ROTATION_LOCK_KEY, isRotationLocked);
            editor.apply();
        }
    }

    private void refreshListView(ListView listView) {
        List<String> items = new ArrayList<>();
        items.add("Rotation Lock" + (isRotationLocked ? " (Enabled)" : ""));
        items.add("Change password");
        items.add("Permissions");
        items.add("About");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }



}