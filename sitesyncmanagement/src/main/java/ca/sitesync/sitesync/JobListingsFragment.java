package ca.sitesync.sitesync;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class JobListingsFragment extends Fragment {

    private static final String TAG = "JobListingsFragment";

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private List<JobItems> jobList;
    private FloatingActionButton addJob;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_job_listings, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.employerJobRv);

        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);

        addJob = view.findViewById(R.id.floatingActionButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(jobAdapter);

        jobAdapter.setOnItemClickListener((jobItem, position) -> {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) return;

            String userId = currentUser.getUid();

            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Cancel Job")
                    .setMessage("Are you sure you want to cancel this job?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        db.collection("Jobs")
                                .document(jobItem.getDocumentId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Alertor.toast(getContext(), "Job deleted");
                                    loadJobs(); // Reload data after deletion
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show()
                                );

                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        addJob.setOnClickListener(view1 -> {
            PostJobsFragment postJobsFragment = new PostJobsFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, postJobsFragment)
                    .commit();
        });

        loadJobs();
        return view;
    }

    private void loadJobs() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Alertor.toast(getContext(), "You must be logged in to view listings.");
            return;
        }

        String userId = currentUser.getUid();

        Query ownerJobsQuery = db.collection("Jobs")
                .whereEqualTo("owner", userId);

        FirestoreUtils.loadJobsFromFirestore(ownerJobsQuery, new FirestoreUtils.OnJobsLoadedListener() {
            @Override
            public void onJobsLoaded(List<JobItems> jobs) {
                jobList.clear();
                jobList.addAll(jobs);

                if (jobAdapter != null) {
                    jobAdapter.notifyDataSetChanged();
                }

                if (jobList.isEmpty()) {
                    Alertor.toast(getContext(), "You have no active job postings.");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error loading owned jobs: ", e);
                Alertor.toast(getContext(), "Failed to load job listings.");
            }
        });
    }
}