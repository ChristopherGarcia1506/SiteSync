package ca.sitesync.sitesync;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Espresso UI tests for the Register screen.
 * Uses real IDs from fragment_register_screen.xml
 */
@RunWith(AndroidJUnit4.class)
public class RegisterScreenEspressoTest {


    @Rule
    public ActivityScenarioRule<RegisterScreen> rule =
            new ActivityScenarioRule<>(RegisterScreen.class);

    @Test
    public void views_are_visible() {
        onView(withId(R.id.RegisterTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.firstname)).check(matches(isDisplayed()));
        onView(withId(R.id.lastname)).check(matches(isDisplayed()));
        onView(withId(R.id.address)).check(matches(isDisplayed()));
        onView(withId(R.id.Organization)).check(matches(isDisplayed()));
        onView(withId(R.id.phonenumber)).check(matches(isDisplayed()));
        onView(withId(R.id.emailInput)).check(matches(isDisplayed()));
        onView(withId(R.id.passwordInput)).check(matches(isDisplayed()));
        onView(withId(R.id.passwordConfirm)).check(matches(isDisplayed()));
        onView(withId(R.id.RegisterButton1)).check(matches(isDisplayed()));
    }

    @Test
    public void can_type_basic_fields() {
        onView(withId(R.id.firstname)).perform(clearText(), typeText("John"), closeSoftKeyboard());
        onView(withId(R.id.lastname)).perform(clearText(), typeText("Doe"), closeSoftKeyboard());
        onView(withId(R.id.address)).perform(clearText(), typeText("123 King St"), closeSoftKeyboard());
        onView(withId(R.id.Organization)).perform(clearText(), typeText("SiteSync Inc"), closeSoftKeyboard());

        onView(withId(R.id.firstname)).check(matches(withText("John")));
        onView(withId(R.id.lastname)).check(matches(withText("Doe")));
        onView(withId(R.id.address)).check(matches(withText("123 King St")));
        onView(withId(R.id.Organization)).check(matches(withText("SiteSync Inc")));
    }

    @Test
    public void phone_accepts_10_digits() {
        onView(withId(R.id.phonenumber)).perform(clearText(), typeText("4165551234"), closeSoftKeyboard());
        onView(withId(R.id.phonenumber)).check(matches(withText("4165551234")));
    }

    @Test
    public void checkbox_toggles() {
        onView(withId(R.id.employerCheckBox)).check(matches(isDisplayed()));
        onView(withId(R.id.employerCheckBox)).perform(click());
        onView(withId(R.id.employerCheckBox)).check(matches(isChecked()));
        onView(withId(R.id.employerCheckBox)).perform(click());
        onView(withId(R.id.employerCheckBox)).check(matches(isNotChecked()));
    }

    @Test
    public void email_password_fields_accept_text() {
        onView(withId(R.id.emailInput)).perform(clearText(), typeText("john@site.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(clearText(), typeText("Aa1!aa"), closeSoftKeyboard());
        onView(withId(R.id.passwordConfirm)).perform(clearText(), typeText("Aa1!aa"), closeSoftKeyboard());

        onView(withId(R.id.emailInput)).check(matches(withText("john@site.com")));
        onView(withId(R.id.passwordInput)).check(matches(withText("Aa1!aa")));
        onView(withId(R.id.passwordConfirm)).check(matches(withText("Aa1!aa")));
    }

    @Test
    public void register_button_click_is_possible() {

        onView(withId(R.id.RegisterButton1)).check(matches(isDisplayed()));
        onView(withId(R.id.RegisterButton1)).perform(click());

    }
}



