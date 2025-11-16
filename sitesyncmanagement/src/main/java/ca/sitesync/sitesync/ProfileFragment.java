/*
Anthony Mancia (N01643670) OCB
Chris Garcia (N01371506) 0CA
Ngoc Le (N01643011) 0CA
Tyler Meira (N01432291) 0CA
*/
package ca.sitesync.sitesync;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {




    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            TextView usersFullName = view.findViewById(R.id.UsersName);
            TextView usersEmail = view.findViewById(R.id.UserEmail);
            TextView usersPhoneNo = view.findViewById(R.id.UserPhone);
            TextView usersOrganization = view.findViewById(R.id.UserOrganization);

            db.collection("Accounts")
                    .document(userId)  // Use document() instead of where() if userId is the document ID
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Get data from document
                                String fullName = document.getString("fullName");
                                String email = document.getString("email");
                                String phoneNumber = document.getString("phoneNumber");
                                String organization = document.getString("organization");

                                // Set the text views with retrieved data
                                if (fullName != null) usersFullName.setText(fullName);
                                if (email != null) usersEmail.setText(email);
                                if (phoneNumber != null) usersPhoneNo.setText(phoneNumber);
                                if (organization != null) usersOrganization.setText(organization);

                            } else {
                                Log.d("ProfileFragment", "No such document");
                            }
                        } else {
                            Log.d("ProfileFragment", "get failed with ", task.getException());
                        }
                    });
        }
        return view;

    }
}