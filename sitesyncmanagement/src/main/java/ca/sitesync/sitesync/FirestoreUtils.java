package ca.sitesync.sitesync;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FirestoreUtils {

    private static final String TAG = "FirestoreUtils";
    private static final String JOBS_COLLECTION = "Jobs";

    static {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder(db.getFirestoreSettings())
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build();
            db.setFirestoreSettings(settings);
            Log.d(TAG, "Firestore offline persistence has been enabled.");
        } catch (Exception e) {
            Log.e(TAG, "Error enabling Firestore offline persistence", e);
        }
    }
    public interface OnJobsLoadedListener {
        void onJobsLoaded(List<JobItems> jobs);
        void onFailure(Exception e);
    }

    public static JobItems mapDocumentToJobItem(QueryDocumentSnapshot doc) {
        String company = doc.getString("Company");
        String description = doc.getString("Description");
        String dbStatus = doc.getString("Status");

        if (company == null || description == null) {
            Log.w(TAG, "Skipping job: Missing Company or Description in document " + doc.getId());
            return null;
        }

        String statusDisplay = (dbStatus != null && dbStatus.equals("Active")) ? "Open" : dbStatus;

        JobItems job = new JobItems(company.trim(), description.trim(), statusDisplay);
        job.setDocumentId(doc.getId());
        return job;
    }

    public static void loadJobsFromFirestore(Query query, OnJobsLoadedListener listener) {
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<JobItems> jobs = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            JobItems job = mapDocumentToJobItem(doc);
                            if (job != null) {
                                jobs.add(job);
                            }
                        }
                        listener.onJobsLoaded(jobs);
                    } else {
                        Log.e(TAG, "Error fetching jobs: ", task.getException());
                        listener.onFailure(task.getException());
                    }
                });
    }

    public static void loadAllJobs(OnJobsLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query allJobsQuery = db.collection(JOBS_COLLECTION);
        loadJobsFromFirestore(allJobsQuery, listener);
    }

}