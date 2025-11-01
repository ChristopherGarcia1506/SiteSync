package ca.sitesync.sitesync;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class JobListingsFragment extends Fragment {

    // this Fragment shows the Listings posted by the "manager"

    private String mParam1;
    private String mParam2;

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
        return inflater.inflate(R.layout.fragment_job_listings, container, false);
    }
}