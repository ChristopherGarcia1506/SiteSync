package ca.sitesync.sitesync;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class JobListingsFragment extends Fragment {

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
                                    Toast.makeText(getContext(), "Job deleted", Toast.LENGTH_SHORT).show();
                                    loadJobs();
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
        if (currentUser == null) return;
        String userId = currentUser.getUid();

        db.collection("Jobs")
                .whereEqualTo("owner", userId)
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) return;

                    jobList.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        JobItems job = new JobItems();
                        job.setCompany(document.getString("Company"));
                        job.setDescription(document.getString("Description"));
                        job.setStatus(document.getString("Status"));
                        job.setDocumentId(document.getId());

                        jobList.add(job);
                    }

                    jobAdapter.updateJobList(jobList);
                });
    }
}
