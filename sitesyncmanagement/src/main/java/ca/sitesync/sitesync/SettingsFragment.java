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
    private ListView settingsListView;
    private ArrayAdapter<String> settingsAdapter;
    private List<String> settingsItems;


    public SettingsFragment() {
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

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        isRotationLocked = sharedPreferences.getBoolean(ROTATION_LOCK_KEY, false);

        if (isRotationLocked && getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsListView = view.findViewById(R.id.listView);

        settingsItems = new ArrayList<>();
        updateSettingsItems();

        settingsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, settingsItems);
        settingsListView.setAdapter(settingsAdapter);

        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = settingsItems.get(position);

                if(selectedItem.startsWith(getString(R.string.logout))){
                    signOut();
                } else if(selectedItem.startsWith(getString(R.string.about))){
                    Toast.makeText(getContext(), R.string.sitesync_v0_01, Toast.LENGTH_SHORT).show();
                } else if (selectedItem.startsWith(getString(R.string.rotation_lock))) {
                    toggleRotationLock();
                    refreshListView();
                } else {
                    Toast.makeText(getContext(), selectedItem + getString(R.string.option_is_not_available_yet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void toggleRotationLock() {
        if (getActivity() != null) {
            if (isRotationLocked) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                isRotationLocked = false;
                Toast.makeText(getContext(), R.string.rotation_unlocked_auto_rotate_enabled, Toast.LENGTH_SHORT).show();
            } else {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                isRotationLocked = true;
                Toast.makeText(getContext(), R.string.rotation_locked_to_portrait_mode, Toast.LENGTH_SHORT).show();
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(ROTATION_LOCK_KEY, isRotationLocked);
            editor.apply();
        }
    }

    private void updateSettingsItems() {
        settingsItems.clear();
        settingsItems.add("Rotation Lock" + (isRotationLocked ? " (Enabled)" : ""));
        settingsItems.add("About");
        settingsItems.add("LogOut");
    }

    private void refreshListView() {
        updateSettingsItems();

        if (settingsAdapter != null) {
            settingsAdapter.notifyDataSetChanged();
        }
    }

    private void signOut() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout2)
                .setMessage(R.string.are_you_sure_you_want_to_logout)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();

        LoginScreen.clearRememberedCredentials(requireContext());

        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
            googleSignInClient.signOut();
        } catch (Exception e) {
            Log.e("LOGOUT", "Google sign-out failed", e);
        }

        Intent intent = new Intent(requireActivity(), LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}