package ca.sitesync.sitesync;

import static ca.sitesync.sitesync.SiteSyncUtils.updateJobsFinishedAnalytics;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ActiveJobsFragment extends Fragment implements JobAdapter.OnItemClickListener {

    private static final String TAG = "ActiveJobsFragment";
    private static final String ACTIVE_STATUS = "Active";

    private RecyclerView recyclerView;
    private JobAdapter adapter;
    private List<JobItems> jobList;

    public ActiveJobsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        jobList = new ArrayList<>();
        adapter = new JobAdapter(jobList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadActiveJobs();
    }


    private void loadActiveJobs() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String employeeEmail = LoginScreen.getRememberedEmail(requireContext());

        if (employeeEmail.isEmpty()) {
            Toast.makeText(requireContext(), R.string.please_log_in_to_see_your_active_jobs, Toast.LENGTH_LONG).show();
            Log.w(TAG, "Employee email is empty, cannot query active jobs.");
            return;
        }

        jobList.clear();

        db.collection("Jobs")
                .whereArrayContains("JobEmployees", employeeEmail)
                .whereEqualTo("Status", ACTIVE_STATUS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String company = doc.getString("Company");
                            String description = doc.getString("Description");
                            String dbStatus = doc.getString("Status");

                            if (company != null && description != null) {
                                JobItems job = new JobItems(company.trim(), description.trim(), dbStatus);
                                job.setDocumentId(doc.getId());
                                jobList.add(job);
                            } else {
                                Log.w(TAG, "Skipping job: Missing Company or Description in document " + doc.getId());
                            }
                        }

                        if (jobList.isEmpty()) {
                            Toast.makeText(requireContext(), R.string.you_are_not_currently_assigned_to_any_active_jobs, Toast.LENGTH_LONG).show();
                        }

                        adapter.notifyDataSetChanged();

                    } else {
                        Log.e(TAG, "Error fetching active jobs: ", task.getException());
                        Toast.makeText(requireContext(), R.string.error_loading_your_jobs, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItemClick(JobItems job, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle(job.getCompany())
                .setMessage("Is this job completed?")
                .setPositiveButton("Complete", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Jobs").document(job.getDocumentId())
                            .update("Status", "InActive")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), job.getCompany() + " job marked as complete.", Toast.LENGTH_SHORT).show();
                                updateJobsFinishedAnalytics(db);
                                jobList.remove(position);
                                adapter.notifyItemRemoved(position);
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update job status.", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Still Working", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


}