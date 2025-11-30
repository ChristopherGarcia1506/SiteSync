package ca.sitesync.sitesync;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class EditProfileFragment extends Fragment {

    private EditText editFirstName, editLastName, editEmail, editPhone, editOrganization;
    private Button saveButton;
    private ImageButton exitButton;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String currentFirstName, currentLastName, currentEmail, currentPhone, currentOrganization;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public static EditProfileFragment newInstance() {
        return new EditProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Initialize Firestore and Firebase Auth
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        initializeViews(view);

        // Load current user data
        loadUserData();

        // Set up save button click listener
        saveButton.setOnClickListener(v -> saveProfileChanges());

        exitButton.setOnClickListener(v -> navigateBackToProfile());
        return view;
    }

    private void initializeViews(View view) {
        editFirstName = view.findViewById(R.id.editFirstName);
        editLastName = view.findViewById(R.id.editLastName);
        editEmail = view.findViewById(R.id.editUserEmail);
        editPhone = view.findViewById(R.id.editUserPhone);
        editOrganization = view.findViewById(R.id.editUserOrganization);
        saveButton = view.findViewById(R.id.saveButton);
        exitButton= view.findViewById(R.id.exitButton);
    }

    private void loadUserData() {
        if (user != null) {
            String userId = user.getUid();

            db.collection("Accounts")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Get data from document and set to EditText fields
                                currentFirstName = document.getString("firstname");
                                currentLastName = document.getString("lastname");
                                currentEmail = document.getString("email");
                                currentPhone = document.getString("phoneNumber");
                                currentOrganization = document.getString("organization");


                                if (currentFirstName != null) editFirstName.setText(currentFirstName);
                                if (currentLastName != null) editLastName.setText(currentLastName);
                                if (currentEmail != null) editEmail.setText(currentEmail);
                                if (currentPhone != null) editPhone.setText(currentPhone);
                                if (currentOrganization != null) editOrganization.setText(currentOrganization);

                            } else {
                                Log.d("EditProfileFragment", "No such document");
                                Alertor.toast(getContext(), "User data not found");
                            }
                        } else {
                            Log.d("EditProfileFragment", "get failed with ", task.getException());
                            Alertor.toast(getContext(), "Failed to load user data");
                        }
                    });
        }
    }

    private void saveProfileChanges() {
        if (user == null) {
            Alertor.toast(getContext(), "User not authenticated");
            return;
        }

        String userId = user.getUid();

        // map to store only the fields that have changes
        Map<String, Object> updates = new HashMap<>();


        String newFirstName = editFirstName.getText().toString().trim();
        String newLastName = editLastName.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newPhone = editPhone.getText().toString().trim();
        String newOrganization = editOrganization.getText().toString().trim();

        // Check if any field has actually been changed
        boolean hasChanges = false;


        if (!newFirstName.equals(currentFirstName != null ? currentFirstName : "")) {
            updates.put("firstname", newFirstName);
            hasChanges = true;
        }

        if (!newLastName.equals(currentLastName != null ? currentLastName : "")) {
            updates.put("lastname", newLastName);
            hasChanges = true;
        }

        if (!newEmail.equals(currentEmail != null ? currentEmail : "")) {
            updates.put("email", newEmail);
            hasChanges = true;
        }

        if (!newPhone.equals(currentPhone != null ? currentPhone : "")) {
            updates.put("phoneNumber", newPhone);
            hasChanges = true;
        }

        if (!newOrganization.equals(currentOrganization != null ? currentOrganization : "")) {
            updates.put("organization", newOrganization);
            hasChanges = true;
        }

        // If no changes were made, show message and return
        if (!hasChanges) {
            Alertor.toast(getContext(), "No changes made");
            return;
        }

        // Show loading indicator
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        // Update Firestore document
        db.collection("Accounts")
                .document(userId)
                .update(updates)
                .addOnCompleteListener(task -> {
                    saveButton.setEnabled(true);
                    saveButton.setText("Save");

                    if (task.isSuccessful()) {
                        Alertor.toast(getContext(), "Profile updated successfully");

                        // Update email in Firebase Auth if email was changed
                        if (updates.containsKey("email") && !newEmail.equals(currentEmail)) {
                            updateAuthEmail(newEmail);
                        } else {
                            navigateBackToProfile();
                        }
                    } else {
                        Log.e("EditProfileFragment", "Update failed", task.getException());
                        Alertor.toast(getContext(), "Failed to update profile: " + task.getException().getMessage());
                    }
                });
    }

    private void updateAuthEmail(String newEmail) {
        user.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("EditProfileFragment", "User email updated in Auth");
                    } else {
                        Log.e("EditProfileFragment", "Failed to update email in Auth", task.getException());
                        // Email update might require re-authentication
                        Alertor.toast(getContext(),
                                "Profile updated but email change requires re-authentication. Please sign in again."
                                );
                    }
                    navigateBackToProfile();
                });
    }
    private void navigateBackToProfile() {
        // Navigate back to ProfileFragment
        ProfileFragment profileFragment = new ProfileFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit();
    }
}