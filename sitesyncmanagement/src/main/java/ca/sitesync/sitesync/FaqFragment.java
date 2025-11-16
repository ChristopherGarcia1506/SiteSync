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

public class FaqFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_faq, container, false);

        RecyclerView rv = v.findViewById(R.id.rvFaq);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<FaqAdapter.FaqItem> items = new ArrayList<>();
        items.add(new FaqAdapter.FaqItem(
                getString(R.string.how_do_i_post_a_job),
                getString(R.string.go_to_the_jobs_tab_tap_the_post_button_fill_in_the_form_and_submit)
        ));
        items.add(new FaqAdapter.FaqItem(
                getString(R.string.why_can_t_i_edit_job_postings),
                getString(R.string.editing_is_controlled_in_permissions_turn_on_allow_editing_job_postings)
        ));
        items.add(new FaqAdapter.FaqItem(
                getString(R.string.i_don_t_want_exit_popups),
                getString(R.string.go_to_permissions_and_turn_off_show_exit_confirmation_dialog)
        ));
        items.add(new FaqAdapter.FaqItem(
                getString(R.string.how_do_i_contact_sitesync),
                getString(R.string.use_the_about_screen_or_email_sitesync_app_com)
        ));

        rv.setAdapter(new FaqAdapter(items));

        return v;
    }
}
