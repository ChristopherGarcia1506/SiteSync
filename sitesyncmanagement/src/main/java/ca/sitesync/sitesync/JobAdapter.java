package ca.sitesync.sitesync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<JobItems> jobList;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(JobItems jobItem, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public JobAdapter(List<JobItems> jobList) {
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.items_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobItems job = jobList.get(position);
        holder.companyName.setText(job.getCompany());
        holder.description.setText(job.getDescription());
        holder.status.setText(job.getStatus());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(job, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void updateJobList(List<JobItems> newJobList) {
        jobList = newJobList;
        notifyDataSetChanged();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView companyName, description, status;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            companyName = itemView.findViewById(R.id.jobTitle);
            description = itemView.findViewById(R.id.jobAddress);
            status = itemView.findViewById(R.id.jobStatus);
        }
    }
}