package ca.sitesync.sitesync;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class PunchLogFragment extends Fragment {

    private static final String TAG = "PunchLogFragment";
    // Firestore instance
    private FirebaseFirestore db;
    // UI elements
    private TextView punchLogTextView;
    private ProgressBar progressBar;
    // ListenerRegistration has been removed as we are using the one-time .get() pattern

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public PunchLogFragment() {
        // Required empty public constructor
    }

    public static PunchLogFragment newInstance(String param1, String param2) {
        PunchLogFragment fragment = new PunchLogFragment();
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
        // 1. Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_punch_log, container, false);

        // 2. Initialize UI components
        punchLogTextView = view.findViewById(R.id.punchLogTextView);
        progressBar = view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 3. Start loading the data when the view is created
        loadPunchLog();
    }

    /**
     * Fetches all documents from the "PunchLog" collection filtered by the current user's email.
     */
    private void loadPunchLog() {
        progressBar.setVisibility(View.VISIBLE);
        punchLogTextView.setText(""); // Clear previous data

        // 1. Get the email of the currently logged-in user using the helper method.
        String currentUserEmail = LoginScreen.getRememberedEmail(requireContext());

        if (currentUserEmail.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            punchLogTextView.setText("Error: User email not found. Please log in again.");
            Log.e(TAG, "Cannot load punch logs: Current user email is empty.");
            return;
        }

        // 2. Query the "PunchLog" collection, filtering by the EmployeeEmail field.
        db.collection("PunchLog")
                .whereEqualTo("EmployeeEmail", currentUserEmail) // Filter applied here
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Check if the result is not null and not empty
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            StringBuilder logData = new StringBuilder();
                            // Format the Timestamps into a readable string
                            // Using SimpleDateFormat to ensure proper formatting of the UTC-5 timestamps
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.getDefault());
                            int entryCount = 1;

                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                try {
                                    // Extract data fields (Timestamps, String, Long)
                                    Timestamp clockInTimestamp = doc.getTimestamp("ClockIn");
                                    Timestamp clockOutTimestamp = doc.getTimestamp("ClockOut");
                                    String employeeEmail = doc.getString("EmployeeEmail");
                                    Long jobId = doc.getLong("JobId");

                                    // Format the output string for the TextView
                                    logData.append("--- Log Entry ").append(entryCount++).append(" (ID: ").append(doc.getId()).append(") ---\n");

                                    // Clock In (Timestamp)
                                    if (clockInTimestamp != null) {
                                        logData.append("  Clock In: ").append(sdf.format(clockInTimestamp.toDate())).append("\n");
                                    } else {
                                        logData.append("  Clock In: N/A\n");
                                    }

                                    // Clock Out (Timestamp - handles null if still clocked in)
                                    if (clockOutTimestamp != null) {
                                        logData.append("  Clock Out: ").append(sdf.format(clockOutTimestamp.toDate())).append("\n");
                                    } else {
                                        logData.append("  Clock Out: Still Clocked In\n");
                                    }

                                    // EmployeeEmail (String)
                                    logData.append("  Email: ").append(employeeEmail != null ? employeeEmail : "N/A").append("\n");
                                    // JobID (Number/Long)
                                    logData.append("  Job ID: ").append(jobId != null ? jobId : "N/A").append("\n\n");

                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing document: " + doc.getId(), e);
                                    logData.append("!!! Error reading data for document ").append(doc.getId()).append(" !!!\n\n");
                                }
                            }
                            // Set the final formatted text to the TextView
                            punchLogTextView.setText(logData.toString());
                        } else {
                            // If the task is successful but returns no documents
                            punchLogTextView.setText("No punch log entries found for " + currentUserEmail + ".");
                        }

                    } else {
                        // Handle the error case (e.g., Firestore connection error, permission denied)
                        Log.e(TAG, "Error loading filtered punch logs: ", task.getException());
                        punchLogTextView.setText("Error loading logs: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    // onStop() is no longer needed to remove a listener since we use a one-time fetch.
}