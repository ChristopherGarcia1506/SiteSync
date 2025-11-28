package ca.sitesync.sitesync;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class SiteSyncUtils {
    private static final String TAG = "SiteSyncUtils";

    private static final String COLLECTION = "Analytics";

    private static final String DOC_24H = "24Hours";
    private static final String DOC_7D = "7Days";

    private static final long MILLIS_24H = 24L * 60 * 60 * 1000;
    private static final long MILLIS_7D = 7L * 24 * 60 * 60 * 1000;


    public static void checkAnalyticsResets(FirebaseFirestore db) {
        checkReset(db, DOC_24H, MILLIS_24H);
        checkReset(db, DOC_7D, MILLIS_7D);
    }

    public static void updateJobsFinished(FirebaseFirestore db) {
        incrementField(db, DOC_24H, "JobsFinished");
        incrementField(db, DOC_7D, "JobsFinished");
    }

    public static void updateJobsAccepted(FirebaseFirestore db) {
        incrementField(db, DOC_24H, "JobsAccepted");
        incrementField(db, DOC_7D, "JobsAccepted");
    }

    private static void incrementField(FirebaseFirestore db, String docName, String field) {
        db.collection(COLLECTION).document(docName)
                .update(field, FieldValue.increment(1))
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, docName + ": " + field + " incremented"))
                .addOnFailureListener(e ->
                        Log.e(TAG, docName + ": Failed to increment " + field, e));
        SiteSyncUtils.checkAnalyticsResets(db);
    }


    private static void checkReset(FirebaseFirestore db, String docName, long intervalMs) {
        db.collection(COLLECTION).document(docName)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Timestamp ts = doc.getTimestamp("resetDate");
                    if (ts == null) {
                        initializeResetDate(db, docName);
                        return;
                    }

                    long last = ts.toDate().getTime();
                    long now = System.currentTimeMillis();
                    long diff = now - last;

                    if (diff >= intervalMs) {
                        resetDocument(db, docName);
                    }

                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed checking reset for " + docName, e));
    }


    private static void initializeResetDate(FirebaseFirestore db, String docName) {
        db.collection(COLLECTION).document(docName)
                .update("resetDate", new Timestamp(new Date()))
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, docName + ": resetDate initialized"))
                .addOnFailureListener(e ->
                        Log.e(TAG, docName + ": Failed to init resetDate", e));
    }

    private static void resetDocument(FirebaseFirestore db, String docName) {
        db.collection(COLLECTION).document(docName)
                .update(
                        "JobsAccepted", 0,
                        "JobsFinished", 0,
                        "resetDate", new Timestamp(new Date())
                )
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, docName + " analytics reset"))
                .addOnFailureListener(e ->
                        Log.e(TAG, docName + ": Failed to reset analytics", e));
    }
}
