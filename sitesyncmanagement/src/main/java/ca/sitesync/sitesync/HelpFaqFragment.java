package ca.sitesync.sitesync;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class HelpFaqFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_faq, container, false);

        RecyclerView rv = v.findViewById(R.id.rvFaq);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<FaqAdapter.FaqItem> items = new ArrayList<>();
        items.add(new FaqAdapter.FaqItem(
                "How do I post a job?",
                "Go to the Jobs tab, tap the + / Post button, fill in the form, and submit."
        ));
        items.add(new FaqAdapter.FaqItem(
                "Why can’t I edit job postings?",
                "Editing is controlled in Permissions. Turn ON “Allow editing job postings”."
        ));
        items.add(new FaqAdapter.FaqItem(
                "I don’t want exit popups.",
                "Go to Permissions and turn OFF “Show exit confirmation dialog”."
        ));
        items.add(new FaqAdapter.FaqItem(
                "How do I contact SiteSync?",
                "Use the About screen or email sitesync@app.com."
        ));

        rv.setAdapter(new FaqAdapter(items));

        return v;
    }
}
