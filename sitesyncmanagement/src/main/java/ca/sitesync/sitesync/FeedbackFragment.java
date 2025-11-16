package ca.sitesync.sitesync;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

        checkCooldown();

        submitButton.setOnClickListener(v -> submitFeedback());

        return view;
    }

    private void submitFeedback() {
        SharedPreferences prefs = getContext().getSharedPreferences("FeedbackPrefs", MODE_PRIVATE);
        long lastSubmissionTime = prefs.getLong("lastSubmissionTime", 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastSubmissionTime < TWENTY_FOUR_HOURS) {
            Toast.makeText(getContext(), "You can only submit once every 24 hours.", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = nameInput.getText().toString();
        String phone = phoneInput.getText().toString();
        String email = emailInput.getText().toString();
        String comment = commentInput.getText().toString();
        float rating = ratingBar.getRating();
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;

        if (!isValidEmail(email)) {
            Toast.makeText(getContext(), "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(getContext(), "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }
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
                        Toast.makeText(getContext(), R.string.feedback_submitted, Toast.LENGTH_SHORT).show();
                // Clear inputs
                nameInput.setText("");
                phoneInput.setText("");
                emailInput.setText("");
                commentInput.setText("");
                ratingBar.setRating(0);

                // Save timestamp
                prefs.edit().putLong("lastSubmissionTime", currentTime).apply();

                // Start cooldown
                startCooldownTimer(TWENTY_FOUR_HOURS);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), R.string.submission_failed, Toast.LENGTH_SHORT).show());
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
    public static boolean isValidEmail(java.lang.String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email != null && email.matches(emailPattern);
    }
    public static boolean isValidPhoneNumber(String phone) {
        // Matches a string that consists of exactly 10 digits.
        String phonePattern = "^[0-9]{10}$";
        return phone != null && !phone.trim().isEmpty() && phone.matches(phonePattern);
    }
}