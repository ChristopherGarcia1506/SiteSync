package ca.sitesync.sitesync;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ActiveJobsFragment extends Fragment {

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
            Toast.makeText(requireContext(), "Please log in to see your active jobs.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Employee email is empty, cannot query active jobs.");
            return;
        }

        jobList.clear();

        db.collection("Jobs")
                .whereArrayContains("JobEmployes", employeeEmail)
                .whereEqualTo("Status", ACTIVE_STATUS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String company = doc.getString("Company");
                            String description = doc.getString("Description");
                            String dbStatus = doc.getString("Status");

                            String statusDisplay = (dbStatus != null && dbStatus.equals("Active")) ? "Active" : "Other";

                            if (company != null && description != null) {
                                JobItems job = new JobItems(company.trim(), description.trim(), statusDisplay);
                                job.setDocumentId(doc.getId());
                                jobList.add(job);
                            } else {
                                Log.w(TAG, "Skipping job: Missing Company or Description in document " + doc.getId());
                            }
                        }

                        if (jobList.isEmpty()) {
                            Toast.makeText(requireContext(), "You are not currently assigned to any active jobs.", Toast.LENGTH_LONG).show();
                        }

                        adapter.notifyDataSetChanged();

                    } else {
                        Log.e(TAG, "Error fetching active jobs: ", task.getException());
                        Toast.makeText(requireContext(), "Error loading your jobs.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}