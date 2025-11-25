package ca.sitesync.sitesync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.VH> {

    private final List<HelpItem> all = new ArrayList<>();
    private final List<HelpItem> shown = new ArrayList<>();

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody;
        ImageView ivChevron;
        View btnRow;
        MaterialButton btnPrimary, btnSecondary;
        public VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvBody = v.findViewById(R.id.tvBody);
            ivChevron = v.findViewById(R.id.ivChevron);
            btnRow = v.findViewById(R.id.btnRow);
            btnPrimary = v.findViewById(R.id.btnPrimary);
            btnSecondary = v.findViewById(R.id.btnSecondary);
        }
    }

    public void setItems(List<HelpItem> items) {
        all.clear(); all.addAll(items);
        shown.clear(); shown.addAll(items);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        shown.clear();
        if (query == null || query.trim().isEmpty()) {
            shown.addAll(all);
        } else {
            String q = query.toLowerCase();
            for (HelpItem i : all) {
                if (i.title.toLowerCase().contains(q) || i.body.toLowerCase().contains(q)) {
                    shown.add(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_help, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        HelpItem item = shown.get(pos);
        h.tvTitle.setText(item.title);
        h.tvBody.setText(item.body);

        h.tvBody.setVisibility(item.expanded ? View.VISIBLE : View.GONE);
        h.btnRow.setVisibility(item.expanded && (item.primary != null || item.secondary != null) ? View.VISIBLE : View.GONE);
        h.ivChevron.setRotation(item.expanded ? 180f : 0f);

        h.itemView.setOnClickListener(v -> {
            item.expanded = !item.expanded;
            notifyItemChanged(h.getBindingAdapterPosition());
        });

        if (item.primary != null) {
            h.btnPrimary.setVisibility(View.VISIBLE);
            h.btnPrimary.setText(item.primary.label());
            h.btnPrimary.setOnClickListener(v -> item.primary.run());
        } else {
            h.btnPrimary.setVisibility(View.GONE);
        }

        if (item.secondary != null) {
            h.btnSecondary.setVisibility(View.VISIBLE);
            h.btnSecondary.setText(item.secondary.label());
            h.btnSecondary.setOnClickListener(v -> item.secondary.run());
        } else {
            h.btnSecondary.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return shown.size(); }
}
