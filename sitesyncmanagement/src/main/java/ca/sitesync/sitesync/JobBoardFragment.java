package ca.sitesync.sitesync;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class JobBoardFragment extends Fragment implements JobAdapter.OnItemClickListener {

    private static final String TAG = "JobBoardFragment";
    private List<JobItems> originalJobList = new ArrayList<>();
    private List<JobItems> filteredJobList = new ArrayList<>();
    private JobAdapter adapter;
    private SearchView searchView;
    private RecyclerView rv;

    public JobBoardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Initialize RecyclerView and Adapter
        rv = v.findViewById(R.id.JobRvBoard);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setHasFixedSize(true);


        adapter = new JobAdapter(filteredJobList);
        adapter.setOnItemClickListener(this);
        rv.setAdapter(adapter);

        // Initialize SearchView
        searchView = v.findViewById(R.id.SearchView);
        setupSearchView();


        loadJobsFromFirestore();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterJobs(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterJobs(newText);
                return true;
            }
        });

        // Clear filter when search is closed
        searchView.setOnCloseListener(() -> {
            if (adapter != null) {
                filteredJobList.clear();
                filteredJobList.addAll(originalJobList);
                adapter.notifyDataSetChanged();
            }
            return false;
        });
    }

    private void filterJobs(String query) {

        filteredJobList.clear();

        if (query.isEmpty()) {
            // if query is empty, show all jobs
            filteredJobList.addAll(originalJobList);
        } else {
            // filter jobs based on query
            String lowerCaseQuery = query.toLowerCase().trim();

            for (JobItems job : originalJobList) {
                // Search in company name, description, and status
                if (job.getCompany().toLowerCase().contains(lowerCaseQuery) ||
                        job.getDescription().toLowerCase().contains(lowerCaseQuery) ||
                        job.getStatus().toLowerCase().contains(lowerCaseQuery)) {
                    filteredJobList.add(job);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // message if no jobs found
        if (filteredJobList.isEmpty() && !query.isEmpty()) {
            Toast.makeText(requireContext(), "No jobs found for: " + query, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadJobsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Jobs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        originalJobList.clear();
                        filteredJobList.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String company = doc.getString("Company");
                            String description = doc.getString("Description");
                            String dbStatus = doc.getString("Status");

                            String statusDisplay = (dbStatus != null && dbStatus.equals("Active"))
                                    ? "Open" : "Closed";

                            if (company != null && description != null) {
                                JobItems job = new JobItems(
                                        company.trim(),
                                        description.trim(),
                                        statusDisplay
                                );

                                job.setDocumentId(doc.getId());
                                originalJobList.add(job);
                            } else {
                                Log.w(TAG, "Skipping job: Missing fields in " + doc.getId());
                            }
                        }

                        // Initially show all jobs
                        filteredJobList.addAll(originalJobList);

                        if (originalJobList.isEmpty()) {
                            Toast.makeText(requireContext(), R.string.no_jobs_found, Toast.LENGTH_SHORT).show();
                        }

                        // Notify adapter that data has changed
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }

                    } else {
                        Log.e(TAG, "Error loading jobs: ", task.getException());
                        Toast.makeText(requireContext(), R.string.error_loading_jobs, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItemClick(JobItems jobItem, int position) {
        checkUserAndJobStatus(jobItem);
    }

    private void checkUserAndJobStatus(JobItems jobItem) {
        String userEmail = LoginScreen.getRememberedEmail(requireContext());
        String jobId = jobItem.getDocumentId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_please_log_in_to_view_job_details, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check employer or employee
        db.collection("Accounts")
                .whereEqualTo("email", userEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(accountSnapshot -> {

                    if (!accountSnapshot.isEmpty()) {

                        DocumentSnapshot userDoc = accountSnapshot.getDocuments().get(0);
                        Boolean isEmployer = userDoc.getBoolean("employer");

                        if (isEmployer != null && isEmployer) {
                            Toast.makeText(requireContext(),
                                    R.string.you_are_registered_as_an_employer_and_cannot_accept_jobs,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        db.collection("Jobs").document(jobId)
                                .get()
                                .addOnSuccessListener(jobDoc -> {

                                    if (!jobDoc.exists()) {
                                        Toast.makeText(requireContext(),
                                                R.string.error_job_document_not_found,
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // SIMPLE ARRAY VERSION
                                    List<String> employees =
                                            (List<String>) jobDoc.get("JobEmployees");

                                    if (employees == null)
                                        employees = new ArrayList<>();

                                    if (employees.contains(userEmail)) {
                                        Toast.makeText(requireContext(),
                                                R.string.you_are_already_an_employee_for_this_job,
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        showAcceptJobDialog(jobItem, userEmail, jobId);
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error checking JobEmployees: ", e));

                    } else {
                        Toast.makeText(requireContext(),
                                R.string.account_data_not_found,
                                Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error checking account status: ", e));
    }

    private void showAcceptJobDialog(JobItems jobItem, String userEmail, String jobId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Would you like to accept this job?");
        builder.setMessage(jobItem.getDescription());

        builder.setPositiveButton(R.string.yes, (DialogInterface dialog, int which) -> {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Jobs").document(jobId)
                    .update("JobEmployees", FieldValue.arrayUnion(userEmail))
                    .addOnSuccessListener(aVoid -> {
                        SiteSyncUtils.updateJobsAccepted(db);

                        Toast.makeText(requireContext(),
                                R.string.job_accepted_you_have_been_added_as_an_employee,
                                Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(),
                                R.string.failed_to_accept_job_try_again,
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error updating JobEmployees array", e);
                    });

        });

        builder.setNegativeButton(R.string.no, (dialog, which) -> {
        });

        builder.show();
    }
}