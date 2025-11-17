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
    private FirebaseFirestore db;
    private TextView punchLogTextView;
    private ProgressBar progressBar;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public PunchLogFragment() {
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
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_punch_log, container, false);

        punchLogTextView = view.findViewById(R.id.punchLogTextView);
        progressBar = view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPunchLog();
    }

    private void loadPunchLog() {
        progressBar.setVisibility(View.VISIBLE);
        punchLogTextView.setText("");

        String currentUserEmail = LoginScreen.getRememberedEmail(requireContext());

        if (currentUserEmail.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            punchLogTextView.setText("Error: User email not found. Please log in again.");
            Log.e(TAG, "Cannot load punch logs: Current user email is empty.");
            return;
        }

        db.collection("PunchLog")
                .whereEqualTo("EmployeeEmail", currentUserEmail)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            StringBuilder logData = new StringBuilder();
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.getDefault());
                            int entryCount = 1;

                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                try {
                                    Timestamp clockInTimestamp = doc.getTimestamp("ClockIn");
                                    Timestamp clockOutTimestamp = doc.getTimestamp("ClockOut");
                                    String employeeEmail = doc.getString("EmployeeEmail");
                                    Long jobId = doc.getLong("JobId");

                                    logData.append("--- Log Entry ").append(entryCount++).append(" (ID: ").append(doc.getId()).append(") ---\n");

                                    if (clockInTimestamp != null) {
                                        logData.append("  Clock In: ").append(sdf.format(clockInTimestamp.toDate())).append("\n");
                                    } else {
                                        logData.append("  Clock In: N/A\n");
                                    }

                                    if (clockOutTimestamp != null) {
                                        logData.append("  Clock Out: ").append(sdf.format(clockOutTimestamp.toDate())).append("\n");
                                    } else {
                                        logData.append("  Clock Out: Still Clocked In\n");
                                    }

                                    logData.append("  Email: ").append(employeeEmail != null ? employeeEmail : "N/A").append("\n");
                                    logData.append("  Job ID: ").append(jobId != null ? jobId : "N/A").append("\n\n");

                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing document: " + doc.getId(), e);
                                    logData.append("!!! Error reading data for document ").append(doc.getId()).append(" !!!\n\n");
                                }
                            }
                            punchLogTextView.setText(logData.toString());
                        } else {
                            punchLogTextView.setText("No punch log entries found for " + currentUserEmail + ".");
                        }

                    } else {
                        Log.e(TAG, "Error loading filtered punch logs: ", task.getException());
                        punchLogTextView.setText("Error loading logs: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }
}