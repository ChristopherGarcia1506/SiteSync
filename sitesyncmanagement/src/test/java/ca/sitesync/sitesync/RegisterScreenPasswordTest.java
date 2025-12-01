package ca.sitesync.sitesync;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * Unit tests for the password rules defined in RegisterScreen.PASSWORD_PATTERN.
 * No Android deps – reads the private Pattern via reflection.
 */

// this class focuses on the specifically on the strength of passwords
public class RegisterScreenPasswordTest {

    private static Pattern PASSWORD_PATTERN;

    @BeforeClass
    public static void grabPattern() throws Exception {
        // Reflect the private static final Pattern PASSWORD_PATTERN in RegisterScreen
        Field f = RegisterScreen.class.getDeclaredField("PASSWORD_PATTERN");
        f.setAccessible(true);
        PASSWORD_PATTERN = (Pattern) f.get(null);
        assertNotNull("PASSWORD_PATTERN must exist on RegisterScreen", PASSWORD_PATTERN);
    }

    private boolean ok(String s) {
        return PASSWORD_PATTERN.matcher(s).matches();
    }


    @Test public void valid_minCombo() { assertTrue(ok("Aa1!aa")); }
    @Test public void valid_longer()    { assertTrue(ok("Z9@abcdEF")); }
    @Test public void valid_manySymbols(){ assertTrue(ok("A1$bcdef!@#")); }



    @Test public void invalid_tooShort_5() { assertFalse(ok("Aa1!a")); }


    @Test public void invalid_noDigit()    { assertFalse(ok("Aa!aaaa")); }
    @Test public void invalid_noUpper()    { assertFalse(ok("a1!aaaa")); }
    @Test public void invalid_noLower()    { assertFalse(ok("A1!AAAA")); }
    @Test public void invalid_noSpecial()  { assertFalse(ok("Aa1aaaa")); }


    @Test public void invalid_spaceInside(){ assertFalse(ok("Aa1! a a")); }



    @Test public void boundary_exactSix_ok(){ assertTrue(ok("A1a!aa")); }
}



