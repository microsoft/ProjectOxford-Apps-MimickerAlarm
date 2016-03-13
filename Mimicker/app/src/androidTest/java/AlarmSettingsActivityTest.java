import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.microsoft.mimickeralarm.R;
import com.microsoft.mimickeralarm.appcore.AlarmMainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AlarmsSettingsActivityTest {
    @Rule
    public ActivityTestRule<AlarmMainActivity> mActivityRule = new ActivityTestRule<>(AlarmMainActivity.class);

    //Make sure you have no alarms before running test
    @Test
    public void alarmSettings_snoozeOptionOn() {
        onView(withId(R.id.fab)).perform(click());
        onView(withText(R.string.pref_button_save)).perform(click());
        onView(withText("Mimicker Alarm")).perform(click());
        onView(withText(R.string.pref_title_snooze)).check(matches(isEnabled()));
        onView(withText(R.string.pref_button_delete)).perform(click());
    }

    @Test
    public void alarmSettings_snoozeOptionOff() {
        onView(withId(R.id.fab)).perform(click());
        onView(withText(R.string.pref_title_snooze)).perform(click());
        onView(withText(R.string.pref_button_save)).perform(click());
        onView(withText("Mimicker Alarm")).perform(click());
        onView(withText(R.string.pref_title_snooze)).check(matches(isEnabled()));
        onView(withText(R.string.pref_button_delete)).perform(click());
    }
}