package ca.sitesync.sitesync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqVH> {

    private final List<FaqItem> data;

    public FaqAdapter(List<FaqItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public FaqVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new FaqVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqVH holder, int position) {
        FaqItem item = data.get(position);
        holder.tvQuestion.setText(item.question);
        holder.tvAnswer.setText(item.answer);
        holder.tvAnswer.setVisibility(item.expanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            item.expanded = !item.expanded;
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class FaqVH extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer;
        FaqVH(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
        }
    }

    public static class FaqItem {
        public String question;
        public String answer;
        public boolean expanded;

        public FaqItem(String q, String a) {
            question = q;
            answer = a;
            expanded = false;
        }
    }
}
