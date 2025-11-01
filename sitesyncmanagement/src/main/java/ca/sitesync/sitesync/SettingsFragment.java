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

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
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
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
        items.add("Permissions");
        items.add("About");
        items.add("LogOut");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        // Add click listener to handle item clicks
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = items.get(position);

                if(selectedItem.startsWith("LogOut")){
                    signOut();
                }
                if (selectedItem.startsWith("Rotation Lock")) {
                    // Handle Rotation Lock functionality
                    toggleRotationLock();
                    // Refresh the list to update the display text
                    refreshListView(listView);
                } else {
                    // Show message for other options
                    Toast.makeText(getContext(), selectedItem + " option is not available yet", Toast.LENGTH_SHORT).show();
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
        items.add("Profile Picture");
        items.add("Manage Accounts");
        items.add("Change password");
        items.add("Permissions");
        items.add("About");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }

    private void signOut() {
        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    private void performLogout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear the Remember Me SharedPreferences
        LoginScreen.clearRememberedCredentials(requireContext());

        // Sign out from Google as well
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
            googleSignInClient.signOut();
        } catch (Exception e) {
            Log.e("LOGOUT", "Google sign-out failed", e);
        }

        // Redirect to login activity
        Intent intent = new Intent(requireActivity(), LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}