/*
Anthony Mancia (N01643670) OCB
Chris Garcia (N01371506) 0CA
Ngoc Le (N01643011) 0CA
Tyler Meira (N01432291) 0CA
*/
package ca.sitesync.sitesync;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JobBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JobBoardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public JobBoardFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
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
        rv.setAdapter(adapter);

        db.collection("Jobs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {

                            // 1. Safely read fields
                            String company = doc.getString("Company");
                            String description = doc.getString("Description");

                            // 2. READ THE NEW FIELD: Status as a String (matching your PostJobsFragment)
                            String dbStatus = doc.getString("Status");

                            // 3. Determine the display status string safely
                            String statusDisplay;
                            // Check if dbStatus is NOT null AND equals "Active" (case-sensitive)
                            if (dbStatus != null && dbStatus.equals("Active")) {
                                statusDisplay = "Opezn";
                            } else {
                                statusDisplay = "Closed";
                            }

                            // 4. Check for nulls and add to list
                            if (company != null && description != null) {

                                String finalCompany = company.trim();
                                String finalDescription = description.trim();

                                // Call JobItems constructor: JobItems(company, description, status)
                                jobList.add(new JobItems(finalCompany, finalDescription, statusDisplay));

                            } else {
                                android.util.Log.w("JobBoard", "Skipping job: Missing Company or Description in document " + doc.getId());
                            }
                        }

                        if (jobList.isEmpty()) {
                            Toast.makeText(requireContext(), R.string.no_jobs_found, Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        android.util.Log.e("JobBoard", "Error loading jobs: ", task.getException());
                        Toast.makeText(requireContext(), R.string.error_loading_jobs, Toast.LENGTH_SHORT).show();
                    }
                });
    }




}