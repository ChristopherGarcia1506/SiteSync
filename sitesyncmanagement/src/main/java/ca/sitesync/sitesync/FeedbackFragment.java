package ca.sitesync.sitesync;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class FeedbackFragment extends Fragment {

    private EditText nameInput, phoneInput, emailInput, commentInput;
    private RatingBar ratingBar;
    private Button submitButton;

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

        submitButton.setOnClickListener(v -> submitFeedback());

        return view;
    }

    private void submitFeedback() {
        String name = nameInput.getText().toString();
        String phone = phoneInput.getText().toString();
        String email = emailInput.getText().toString();
        String comment = commentInput.getText().toString();
        float rating = ratingBar.getRating();
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;

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
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getContext(), R.string.feedback_submitted, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), R.string.submission_failed, Toast.LENGTH_SHORT).show());
    }
}