package ca.sitesync.sitesync;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
// Check is Overflow works and open both FAQ and About
@RunWith(AndroidJUnit4.class)
public class OverflowNavigationUiTest {

    @Rule public ActivityScenarioRule<MainActivity> rule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test public void openFaq_fromOverflow() {
        onView(withContentDescription("More options")).perform(click());
        onView(withText("FAQ")).perform(click());
        onView(withText("How do I post a job?")).perform(click());
        onView(withText(R.string.go_to_the_jobs_tab_tap_the_post_button_fill_in_the_form_and_submit))
                .check(matches(isDisplayed()));
    }

    @Test public void openAbout_fromOverflow() {
        onView(withContentDescription("More options")).perform(click());
        onView(withText("About")).perform(click());
        onView(withText("© 2025 SiteSync – SiteSync Inc."))
                .check(matches(isDisplayed()));
    }
}
