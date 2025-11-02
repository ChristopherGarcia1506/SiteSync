package ca.sitesync.sitesync;

public class JobItems {
    private String company;
    private String description;
    private String status;
    private String employerId;
    private String jobId;

    public JobItems() {
    }

    public JobItems() {
    }

    public JobItems(String company, String description, String status) {
        this.company = company;
        this.description = description;
        this.status = status;
    }

    public String getCompany() { return company; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }


    public void setCompany(String title) { this.company = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }

}