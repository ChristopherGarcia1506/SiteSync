package ca.sitesync.sitesync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PastJobsFragment extends Fragment {

    private RecyclerView recyclerView;
    private JobAdapter adapter;
    private List<JobItems> jobList;

    public PastJobsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        //adding details
        jobList = new ArrayList<>();
        jobList.add(new JobItems("Asphalt Spraying", "3 Eireen Quay", "Completed"));
        adapter = new JobAdapter(jobList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}