package ca.sitesync.sitesync;

public class HelpItem {
    public final String title;
    public final String body;
    public final Action primary;     // optional deep link button
    public final Action secondary;   // optional secondary button
    public boolean expanded;

    public interface Action {
        void run();
        String label();
    }

    public HelpItem(String title, String body, Action primary, Action secondary) {
        this.title = title;
        this.body = body;
        this.primary = primary;
        this.secondary = secondary;
        this.expanded = false;
    }
}
