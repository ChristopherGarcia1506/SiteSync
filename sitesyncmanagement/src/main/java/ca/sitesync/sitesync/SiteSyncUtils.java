package ca.sitesync.sitesync;

import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class SiteSyncUtils {
    private static final String TAG = "SiteSyncUtils";

    public static void updateJobsFinishedAnalytics(FirebaseFirestore db) {
        db.collection("Analytics").document("SiteSync")
                .update("JobsFinished", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Analytics JobsFinished counter updated successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update Analytics JobsFinished counter: ", e);
                });
    }
}
