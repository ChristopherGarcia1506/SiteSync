package ca.sitesync.sitesync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HelpFragment extends Fragment {

    private HelpAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, container, false);

        RecyclerView list = v.findViewById(R.id.helpList);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HelpAdapter();
        list.setAdapter(adapter);

        SearchView search = v.findViewById(R.id.searchHelp);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { adapter.filter(q); return true; }
            @Override public boolean onQueryTextChange(String q) { adapter.filter(q); return true; }
        });

        adapter.setItems(buildHelpItems());

        return v;
    }

    private List<HelpItem> buildHelpItems() {
        List<HelpItem> items = new ArrayList<>();

        items.add(new HelpItem(
                getString(R.string.help_quick_start_title),
                getString(R.string.help_quick_start_body),
                null, null
        ));

        items.add(new HelpItem(
                getString(R.string.help_bottom_nav_title),
                getString(R.string.help_bottom_nav_body),
                null, null
        ));

        items.add(new HelpItem(
                getString(R.string.help_permissions_title),
                getString(R.string.help_permissions_body),
                new HelpItem.Action() {
                    @Override public void run() {
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new PermissionsFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                    @Override public String label() { return getString(R.string.open_permissions); }
                },
                null
        ));

        items.add(new HelpItem(
                getString(R.string.help_trouble_jobs_title),
                getString(R.string.help_trouble_jobs_body),
                null, null
        ));

        items.add(new HelpItem(
                getString(R.string.help_settings_title),
                getString(R.string.help_settings_body),
                new HelpItem.Action() {
                    @Override public void run() {
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new SettingsFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                    @Override public String label() { return getString(R.string.open_settings); }
                },
                null
        ));

        return items;
    }
}
