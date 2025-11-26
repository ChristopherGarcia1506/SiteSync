package ca.sitesync.sitesync;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppNavigationUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule =
            new ActivityScenarioRule<>(MainActivity.class);


    //  Bottom nav buttons render
    @Test public void bottom_nav_items_visible() {
        onView(withId(R.id.nav_home)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_jobs)).check(matches(isDisplayed()));
    }

    @Test public void openFaq_fromOverflow() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
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

    @Test public void openDrawer_and_clickSettings() {

        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_settings));
        onView(withId(R.id.nav_settings))
                .check(matches(isDisplayed()));
    }










}
