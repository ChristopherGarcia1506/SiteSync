package ca.sitesync.sitesync;

public class JobItems {
    private String title;
    private String address;
    private String status;

    public JobItems(String title, String address, String status) {
        this.title = title;
        this.address = address;
        this.status = status;
    }

    // Getters
    public String getTitle() { return title; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
}