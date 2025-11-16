package ca.sitesync.sitesync;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JobBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JobBoardFragment extends Fragment implements JobAdapter.OnItemClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private static final String TAG = "JobBoardFragment";

    public JobBoardFragment() {
        // Required empty public constructor
    }

    public static JobBoardFragment newInstance(String param1, String param2) {
        JobBoardFragment fragment = new JobBoardFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_board, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        RecyclerView rv = v.findViewById(R.id.JobRvBoard);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setHasFixedSize(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<JobItems> jobList = new ArrayList<>();
        JobAdapter adapter = new JobAdapter(jobList);

        adapter.setOnItemClickListener(this);

        rv.setAdapter(adapter);

        db.collection("Jobs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {

                            String company = doc.getString("Company");
                            String description = doc.getString("Description");
                            String dbStatus = doc.getString("Status");

                            String statusDisplay;
                            if (dbStatus != null && dbStatus.equals("Active")) {
                                statusDisplay = "Opezn";
                            } else {
                                statusDisplay = "Closed";
                            }

                            if (company != null && description != null) {

                                String finalCompany = company.trim();
                                String finalDescription = description.trim();

                                JobItems job = new JobItems(finalCompany, finalDescription, statusDisplay);
                                job.setDocumentId(doc.getId()); // Set the Firestore Document ID

                                jobList.add(job);

                            } else {
                                android.util.Log.w(TAG, "Skipping job: Missing Company or Description in document " + doc.getId());
                            }
                        }

                        if (jobList.isEmpty()) {
                            Toast.makeText(requireContext(), R.string.no_jobs_found, Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        android.util.Log.e(TAG, "Error loading jobs: ", task.getException());
                        Toast.makeText(requireContext(), R.string.error_loading_jobs, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItemClick(JobItems jobItem, int position) {
        // Delegate to the main logic method
        checkUserAndJobStatus(jobItem);
    }

    private void checkUserAndJobStatus(JobItems jobItem) {
        String userEmail = LoginScreen.getRememberedEmail(requireContext());
        String jobId = jobItem.getDocumentId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Error: Please log in to view job details.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (jobId == null || jobId.isEmpty()) {
            Toast.makeText(requireContext(), "Error: Job identifier not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Check Employer Status
        db.collection("Accounts")
                .whereEqualTo("email", userEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
                        Boolean isEmployer = userDoc.getBoolean("employer");

                        // Stop execution for employers
                        if (isEmployer != null && isEmployer) {
                            Toast.makeText(requireContext(), "You are registered as an Employer and cannot accept jobs.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 2. Check if already an Employee
                        db.collection("Jobs").document(jobId)
                                .get()
                                .addOnSuccessListener(jobDoc -> {
                                    if (jobDoc.exists()) {
                                        List<String> employees = (List<String>) jobDoc.get("JobEmployes");
                                        if (employees == null) {
                                            employees = new ArrayList<>();
                                        }

                                        if (employees.contains(userEmail)) {
                                            Toast.makeText(requireContext(), "You are already an employee for this job.", Toast.LENGTH_LONG).show();
                                        } else {
                                            // 3. Show confirmation dialog
                                            showAcceptJobDialog(jobItem, userEmail, jobId);
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), "Error: Job document not found.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Error checking job status.", Toast.LENGTH_SHORT).show();
                                    android.util.Log.e(TAG, "Error checking JobEmployes: ", e);
                                });

                    } else {
                        Toast.makeText(requireContext(), "Account data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error checking account status.", Toast.LENGTH_SHORT).show();
                    android.util.Log.e(TAG, "Error checking user employer status: ", e);
                });
    }

    private void showAcceptJobDialog(JobItems jobItem, String userEmail, String jobId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Would you like to accept this job?");
        builder.setMessage(jobItem.getDescription());

        // Yes button: Perform Database Update
        builder.setPositiveButton("Yes", (dialog, which) -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Jobs").document(jobId)
                    .update("JobEmployes", FieldValue.arrayUnion(userEmail))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Job accepted! You have been added as an employee.", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to accept job. Try again.", Toast.LENGTH_LONG).show();
                        android.util.Log.e(TAG, "Error updating job employes", e);
                    });
        });

        // No button: Dismiss dialog
        builder.setNegativeButton("No", (dialog, which) -> {
            Toast.makeText(requireContext(), "You clicked No!", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }
}