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
        holder.title.setText(job.getCompany());
        holder.address.setText(job.getDescription());
        holder.status.setText(job.getLocation());
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView title, address, status;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.jobTitle);
            address = itemView.findViewById(R.id.jobAddress);
            status = itemView.findViewById(R.id.jobStatus);
        }
    }

    //method to update list
    public void updateJobList(List<JobItems> newJobList) {
        jobList.clear();
        jobList.addAll(newJobList);
        notifyDataSetChanged();
    }
}