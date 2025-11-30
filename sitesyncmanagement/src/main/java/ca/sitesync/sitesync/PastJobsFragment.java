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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PastJobsFragment extends Fragment {

    private static final String TAG = "PastJobsFragment";
    private static final String INACTIVE_STATUS = "InActive";

    private RecyclerView recyclerView;
    private JobAdapter adapter;
    private List<JobItems> jobList;

    public PastJobsFragment() {}

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
        loadPastJobs();
    }

    private void loadPastJobs() {
        String employeeEmail = LoginScreen.getRememberedEmail(requireContext());

        if (employeeEmail == null || employeeEmail.isEmpty()) {
            Toast.makeText(requireContext(),
                    R.string.please_log_in_to_see_your_past_jobs,
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "Employee email is empty, cannot query past jobs.");
            return;
        }

        jobList.clear();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query pastJobsQuery = db.collection("Jobs")
                .whereArrayContains("JobEmployees", employeeEmail)
                .whereEqualTo("Status", INACTIVE_STATUS);


        FirestoreUtils.loadJobsFromFirestore(pastJobsQuery, new FirestoreUtils.OnJobsLoadedListener() {
            @Override
            public void onJobsLoaded(List<JobItems> jobs) {
                jobList.clear();
                jobList.addAll(jobs);

                if (jobList.isEmpty()) {
                    Toast.makeText(requireContext(),
                            R.string.you_do_not_have_any_inactive_jobs,
                            Toast.LENGTH_LONG).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching past jobs: ", e);
                Toast.makeText(requireContext(),
                        R.string.error_loading_your_jobs,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}