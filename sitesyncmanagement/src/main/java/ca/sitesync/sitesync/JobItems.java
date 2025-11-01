package ca.sitesync.sitesync;

public class JobItems {
    private String title;
    private String address;
    private String status;
    private String employerId;
    private String jobId;

    public JobItems() {
    }

    public JobItems(String title, String address, String status) {
        this.title = title;
        this.address = address;
        this.status = status;
    }
    public JobItems(String title, String address, String status, String employerId) {
        this.title = title;
        this.address = address;
        this.status = status;
        this.employerId = employerId;
    }
    // Getters
    public String getTitle() { return title; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }

    public void setTitle(String title) { this.title = title; }
    public void setAddress(String address) { this.address = address; }
    public void setStatus(String status) { this.status = status; }
    public void setEmployerId(String employerId) { this.employerId = employerId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
}