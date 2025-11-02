package ca.sitesync.sitesync;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class JobListingsFragment extends Fragment {

    // this Fragment shows the Listings posted by the "manager"



    public JobListingsFragment() {
        // Required empty public constructor
    }

    public static JobListingsFragment newInstance(String param1, String param2) {
        JobListingsFragment fragment = new JobListingsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_job_listings, container, false);
        ImageButton imgBtn = view.findViewById(R.id.addListingBtn);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create the fragment you want to load
                PostJobsFragment postJobsFragment = new PostJobsFragment();


                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, postJobsFragment)
                        .commit();
            }
        });

        return view;
    }


}