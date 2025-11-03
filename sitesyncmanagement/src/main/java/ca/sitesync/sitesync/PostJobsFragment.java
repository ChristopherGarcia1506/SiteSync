package ca.sitesync.sitesync;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class PostJobsFragment extends Fragment {

    private static final String TAG = "PostJobsFragment";
    private FirebaseFirestore db;

    private String currentUserEmail = "test@company.com";
    private String currentCompany = "SiteSync Corp";


    public PostJobsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_jobs, container, false);

        ImageButton exitImgBtn = view.findViewById(R.id.clostBtn);

        exitImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create the fragment you want to load
                JobListingsFragment jobListingsFragment = new JobListingsFragment();


                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, jobListingsFragment)
                        .commit();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        EditText companyInput = view.findViewById(R.id.editTextCompany);
        EditText descriptionInput = view.findViewById(R.id.editTextDescription);
        EditText payInput = view.findViewById(R.id.editTextPay);
        EditText locationInput = view.findViewById(R.id.exitTextLocation);
        Button postButton = view.findViewById(R.id.button2);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String company = companyInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();
                String pay = payInput.getText().toString().trim();
                String location = locationInput.getText().toString().trim();

                if (description.isEmpty() || location.isEmpty() || pay.isEmpty() || company.isEmpty()) {
                    Toast.makeText(getContext(), R.string.please_fill_out_all_required_fields, Toast.LENGTH_SHORT).show();
                    return;
                }

                getNextJobIDAndPost(description, location, pay, company);
            }
        });
    }

    private void getNextJobIDAndPost(String description, String location, String pay, String company) {

        db.collection("Jobs")
                .orderBy("JobID", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            long nextJobID = 1;

                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {

                                DocumentSnapshot lastDocument = querySnapshot.getDocuments().get(0);

                                Long currentMaxID = lastDocument.getLong("JobID");

                                if (currentMaxID != null) {
                                    nextJobID = currentMaxID + 1;
                                }
                            }

                            postJobToFirestore(nextJobID, description, location, pay, company);

                        } else {
                            Log.e(TAG, "Error fetching max JobID: ", task.getException());
                            Toast.makeText(getContext(), R.string.failed_to_generate_job_id_please_try_again, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void postJobToFirestore(long jobID, String description, String location, String pay, String company) {

        Map<String, Object> job = new HashMap<>();
        job.put("Description", description);
        job.put("Location", location);
        job.put("Pay", pay);
        job.put("Company", company);
        job.put("Email", currentUserEmail);
        job.put("JobID", jobID);
        job.put("Status", "Active");

        db.collection("Jobs")
                .add(job)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Job successfully posted with ID: " + documentReference.getId() + " and JobID: " + jobID);
                        Toast.makeText(getContext(), R.string.job_posted_successfully, Toast.LENGTH_LONG).show();

                        if (getView() != null) {
                            ((EditText) getView().findViewById(R.id.editTextCompany)).setText("");
                            ((EditText) getView().findViewById(R.id.editTextDescription)).setText("");
                            ((EditText) getView().findViewById(R.id.editTextPay)).setText("");
                            ((EditText) getView().findViewById(R.id.exitTextLocation)).setText("");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error posting job", e);
                        Toast.makeText(getContext(), R.string.failed_to_post_job, Toast.LENGTH_LONG).show();
                    }
                });
    }
}