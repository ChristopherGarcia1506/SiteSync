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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.concurrent.ExecutionException;

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

        if (LoginScreen.isEmployer) {
            loadEmployerLogs(currentUserEmail);
        } else {
            loadEmployeeLogs(currentUserEmail);
        }
    }

    private void loadEmployeeLogs(String currentUserEmail) {
        db.collection("PunchLog")
                .whereEqualTo("EmployeeEmail", currentUserEmail)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    processLogResults(task, currentUserEmail);
                });
    }

    private void loadEmployerLogs(String currentUserEmail) {
        // Step 1: Get the employer's Document ID (owner ID)
        db.collection("Accounts")
                .whereEqualTo("email", currentUserEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult().isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        punchLogTextView.setText("Error: Could not retrieve employer account details.");
                        Log.e(TAG, "Failed to find employer document.", task.getException());
                        return;
                    }

                    String employerOwnerId = task.getResult().getDocuments().get(0).getId();

                    db.collection("Jobs")
                            .whereEqualTo("owner", employerOwnerId)
                            .get()
                            .addOnCompleteListener(jobTask -> {
                                if (!jobTask.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    punchLogTextView.setText("Error: Could not retrieve employer's jobs.");
                                    Log.e(TAG, "Failed to find employer jobs.", jobTask.getException());
                                    return;
                                }

                                List<Long> jobIds = new ArrayList<>();
                                for (QueryDocumentSnapshot jobDoc : jobTask.getResult()) {
                                    Long jobId = jobDoc.getLong("JobID");
                                    if (jobId != null) {
                                        jobIds.add(jobId);
                                    }
                                }

                                if (jobIds.isEmpty()) {
                                    progressBar.setVisibility(View.GONE);
                                    punchLogTextView.setText("No punch logs found. You haven't created any active jobs.");
                                    return;
                                }

                                if (jobIds.size() > 10) {
                                    Log.w(TAG, "Employer has more than 10 jobs. Only the first 10 will be queried.");
                                    jobIds = jobIds.subList(0, 10);
                                }

                                db.collection("PunchLog")
                                        .whereIn("JobId", jobIds)
                                        .get()
                                        .addOnCompleteListener(punchLogTask -> {
                                            progressBar.setVisibility(View.GONE);
                                            processLogResults(punchLogTask, "jobs you manage");
                                        });
                            });
                });
    }

    private void processLogResults(@NonNull Task<QuerySnapshot> task, String filterCriterion) {
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

                        logData.append(getString(R.string.log_entry)).append(entryCount++).append(" (ID: ").append(doc.getId()).append(") ---\n");

                        if (clockInTimestamp != null) {
                            logData.append(getString(R.string.clock_in)).append(sdf.format(clockInTimestamp.toDate())).append("\n");
                        } else {
                            logData.append(getString(R.string.clock_in_n_a));
                        }

                        if (clockOutTimestamp != null) {
                            logData.append(getString(R.string.clock_out)).append(sdf.format(clockOutTimestamp.toDate())).append("\n");
                        } else {
                            logData.append(getString(R.string.clock_out_still_clocked_in));
                        }

                        logData.append("  Email: ").append(employeeEmail != null ? employeeEmail : "N/A").append("\n");
                        logData.append("  Job ID: ").append(jobId != null ? jobId : "N/A").append("\n\n");

                    } catch (Exception e) {
                        Log.e(TAG, "Error processing document: " + doc.getId(), e);
                        logData.append(getString(R.string.error_reading_data_for_document)).append(doc.getId()).append(" !!!\n\n");
                    }
                }
                punchLogTextView.setText(logData.toString());
            } else {
                punchLogTextView.setText(getString(R.string.no_punch_log_entries_found_for) + filterCriterion + ".");
            }

        } else {
            Log.e(TAG, "Error loading filtered punch logs: ", task.getException());
            punchLogTextView.setText(getString(R.string.error_loading_logs) + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
        }
    }
}