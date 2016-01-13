package com.microsoft.mimicker.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceManager;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.microsoft.mimicker.BuildConfig;
import com.microsoft.mimicker.R;
import com.microsoft.mimicker.appcore.AlarmApplication;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

public class GeneralUtilities {
    public static void enableLinks(TextView view) {
        if (view != null) {
            view.setMovementMethod(LinkMovementMethod.getInstance());
            GeneralUtilities.stripUnderlines(view);
        }
    }

    public static Uri defaultRingtone() {
        return Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.mimicker_ringtone);
    }

    public static void stripUnderlines(TextView textView) {
        Spannable s = (Spannable)textView.getText();
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    public static void registerCrashReport(Context context){
        final String hockeyappToken = KeyUtilities.getToken(context, "hockeyapp");
        if (!BuildConfig.DEBUG) {
            CrashManager.register(context, hockeyappToken, new CrashManagerListener() {
                public boolean shouldAutoUploadCrashes() {
                    return true;
                }
            });
        }
        else {
            CrashManager.register(context, hockeyappToken);
        }
    }

    // http://stackoverflow.com/questions/4096851/remove-underline-from-links-in-textview-android
    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }

    public static void setLockScreenFlags(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    public static void clearLockScreenFlags(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    public static void showFragment(FragmentManager fragmentManager, Fragment fragment,
                              String fragmentTag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, fragmentTag);
        transaction.commit();
    }

    public static void showFragmentFromRight(FragmentManager fragmentManager, Fragment fragment,
                                       String fragmentTag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.fragment_container, fragment, fragmentTag);
        transaction.commit();
    }

    public static void showFragmentFromLeft(FragmentManager fragmentManager, Fragment fragment,
                                      String fragmentTag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.fragment_container, fragment, fragmentTag);
        transaction.commit();
    }

    public static int getDurationSetting(int settingKeyResId,
                                         int defaultSettingStringResId,
                                         int defaultDuration) {
        Context context = AlarmApplication.getAppContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String durationPreference = preferences.getString(
                context.getResources().getString(settingKeyResId),
                context.getResources().getString(defaultSettingStringResId));

        int duration = defaultDuration;
        try {
            duration = Integer.parseInt(durationPreference);
        } catch (NumberFormatException e) {
            Logger.trackException(e);
        }

        return duration;
    }

    @SuppressWarnings("deprecation")
    public static boolean deviceHasFrontFacingCamera() {
        return hasDeviceCameraWithDirection(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    @SuppressWarnings("deprecation")
    public static boolean deviceHasRearFacingCamera() {
        return hasDeviceCameraWithDirection(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    @SuppressWarnings("deprecation")
    private static boolean hasDeviceCameraWithDirection(int cameraDirection) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {

            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == cameraDirection) {
                return true;
            }
        }
        return false;
    }
}
