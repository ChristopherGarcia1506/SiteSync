package ca.sitesync.sitesync;

public class JobItems {
    private String company;
    private String description;
    private String location;

    public JobItems(String company, String description, String location) {
        this.company = company;
        this.description = description;
        this.location = location;
    }

    public String getCompany() { return company; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
}
