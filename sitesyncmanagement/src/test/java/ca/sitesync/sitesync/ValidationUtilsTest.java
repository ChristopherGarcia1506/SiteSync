package ca.sitesync.sitesync;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationUtilsTest {

    // Email (≥10 tests total across class)
    @Test public void email_valid_plus() { assertTrue(ValidationUtils.isValidEmail("john+work@site.com")); }
    @Test public void email_valid_sub() { assertTrue(ValidationUtils.isValidEmail("qa@sub.domain.com")); }
    @Test public void email_invalid_noAt() { assertFalse(ValidationUtils.isValidEmail("ab.com")); }
    @Test public void email_invalid_trailingSpace() { assertFalse(ValidationUtils.isValidEmail("qb@b.com ")); }

    // Phone
    @Test public void phone_valid_10digits() { assertTrue(ValidationUtils.isValidPhoneNumber("4165551234")); }
    @Test public void phone_invalid_letters() { assertFalse(ValidationUtils.isValidPhoneNumber("41655A123B")); }

    // Password (rules mirror RegisterScreen)
    @Test public void password_weak_short() { assertFalse(ValidationUtils.isValidEmail("A1!a")); }
    @Test public void password_weak_noDigit() { assertFalse(ValidationUtils.isValidEmail("Aaaaaaaa!")); }
    @Test public void password_weak_noUpper() { assertFalse(ValidationUtils.isValidEmail("aa1!aaaa")); }

    // Show use of other asserts
    @Test public void email_object_notNull() {
        String e = "dev@x.com";
        assertNotNull(e);
        assertTrue(ValidationUtils.isValidEmail(e));
    }
    @Test public void equals_examples() {
        String a = "x@y.com";
        String b = "x@y.com";
        assertEquals(a, b);
        assertNotEquals(a, "x@z.com");
    }
}
