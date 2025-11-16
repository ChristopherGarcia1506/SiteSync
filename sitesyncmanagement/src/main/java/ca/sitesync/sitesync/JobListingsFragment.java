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
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class JobListingsFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;
    private List<JobItems> jobList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job_listings, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.employerJobRv);
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(jobAdapter);

        // Add button to post new job
        view.findViewById(R.id.addListingBtn).setOnClickListener(v -> {
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
        assert currentUser != null;
        String userId = currentUser.getUid();

        db.collection(getString(R.string.jobs))
                .whereEqualTo("owner", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        jobList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String company = document.getString(getString(R.string.company));
                            String description = document.getString(getString(R.string.description));
                            String status = document.getString(getString(R.string.status));

                            JobItems job = new JobItems(company, description, status);
                            jobList.add(job);
                        }
                        jobAdapter.updateJobList(jobList);
                    }
                });
    }
}