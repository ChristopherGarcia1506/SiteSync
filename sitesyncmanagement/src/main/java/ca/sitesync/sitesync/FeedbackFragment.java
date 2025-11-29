package ca.sitesync.sitesync;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class FeedbackFragment extends Fragment {

    private EditText nameInput, phoneInput, emailInput, commentInput;
    private RatingBar ratingBar;
    private Button submitButton;
    private TextView timerTextView;
    private ProgressBar progressBar;
    private static final long TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        nameInput = view.findViewById(R.id.nameInput);
        phoneInput = view.findViewById(R.id.phonenumber);
        emailInput = view.findViewById(R.id.emailInput);
        commentInput = view.findViewById(R.id.commentInput);
        ratingBar = view.findViewById(R.id.ratingBar);
        submitButton = view.findViewById(R.id.submitButton);
        timerTextView = view.findViewById(R.id.timerTextView);
        progressBar = view.findViewById(R.id.progressBar); // Initialize ProgressBar

        checkCooldown();

        submitButton.setOnClickListener(v -> submitFeedback());
        return view;
    }

    private void submitFeedback() {
        SharedPreferences prefs = getContext().getSharedPreferences("FeedbackPrefs", MODE_PRIVATE);
        long lastSubmissionTime = prefs.getLong("lastSubmissionTime", 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastSubmissionTime < TWENTY_FOUR_HOURS) {
            Toast.makeText(getContext(), R.string.you_can_only_submit_once_every_24_hours, Toast.LENGTH_SHORT).show();
            return;
        }
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String comment = commentInput.getText().toString().trim();
        float rating = ratingBar.getRating();
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        if (name.isEmpty()) {
            Toast.makeText(getContext(), R.string.please_fill_out_name_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (comment.isEmpty()) {
            Toast.makeText(getContext(), R.string.please_fill_out_comment_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            Toast.makeText(getContext(), R.string.please_enter_a_valid_email_address, Toast.LENGTH_SHORT).show();
            return;
        }
        if (rating == 0) {
            Toast.makeText(getContext(), R.string.please_provide_a_rating, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationUtils.isValidPhoneNumber(phone)) {
            Toast.makeText(getContext(), "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE); // Show progress bar
        submitButton.setEnabled(false); // Disable button

        // Handler for the 5-second delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            Map<String, Object> feedback = new HashMap<>();
            feedback.put("name", name);
            feedback.put("phone", phone);
            feedback.put("email", email);
            feedback.put("comment", comment);
            feedback.put("rating", rating);
            feedback.put("deviceModel", deviceModel);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Feedback")
                    .add(feedback)
                    .addOnSuccessListener(documentReference -> {
                        // --- On Success ---
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                        showSuccessDialog();

                        // Clear inputs
                        nameInput.setText("");
                        phoneInput.setText("");
                        emailInput.setText("");
                        commentInput.setText("");
                        ratingBar.setRating(0);

                        // Save timestamp and start cooldown
                        prefs.edit().putLong("lastSubmissionTime", System.currentTimeMillis()).apply();
                        startCooldownTimer(TWENTY_FOUR_HOURS);
                    })
                    .addOnFailureListener(e -> {
                        // --- On Failure ---
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                        submitButton.setEnabled(true); // Re-enable button
                        Toast.makeText(getContext(), "Submission failed, please try again.", Toast.LENGTH_SHORT).show();
                    });

        }, 5000); // 5000 milliseconds = 5 seconds
    }

    private void showSuccessDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Feedback Submitted")
                .setMessage("Thank you! Your feedback has been received successfully.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void checkCooldown() {
        SharedPreferences prefs = getContext().getSharedPreferences("FeedbackPrefs", MODE_PRIVATE);
        long lastSubmissionTime = prefs.getLong("lastSubmissionTime", 0);
        long currentTime = System.currentTimeMillis();

        long remainingTime = TWENTY_FOUR_HOURS - (currentTime - lastSubmissionTime);
        if (remainingTime > 0) {
            startCooldownTimer(remainingTime);
        } else {
            submitButton.setEnabled(true);
            timerTextView.setText("");
        }
    }

    private void startCooldownTimer(long remainingTime) {
        submitButton.setEnabled(false);

        new CountDownTimer(remainingTime, 1000) {
            @SuppressLint("DefaultLocale")
            public void onTick(long millisUntilFinished) {
                long hours = (millisUntilFinished / (1000 * 60 * 60));
                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                timerTextView.setText(
                        String.format("%02d:%02d:%02d remaining until next submission", hours, minutes, seconds)
                );
            }

            public void onFinish() {
                submitButton.setEnabled(true);
                timerTextView.setText("You can submit your feedback!");
            }
        }.start();
    }
}