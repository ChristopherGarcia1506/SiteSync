package ca.sitesync.sitesync;

public class ValidationUtils {
    public static boolean isValidEmail(java.lang.String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email != null && email.matches(emailPattern);
    }
}
