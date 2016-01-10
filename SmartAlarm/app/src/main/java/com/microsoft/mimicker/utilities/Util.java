package com.microsoft.mimicker.utilities;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.widget.TextView;

import com.microsoft.mimicker.BuildConfig;
import com.microsoft.mimicker.R;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

public class Util {
    public static String getToken(Context caller, String resource) {
        String token = null;
        try {
            ApplicationInfo ai = caller.getPackageManager().getApplicationInfo(caller.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            token = bundle.getString("com.microsoft.mimicker.token." + resource);
        }
        catch (Exception ex) {
            Logger.trackException(ex);
        }
        return token;
    }

    public static void enableLinks(TextView view) {
        if (view != null) {
            view.setMovementMethod(LinkMovementMethod.getInstance());
            Util.stripUnderlines(view);
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
        final String hockeyappToken = KeyUtil.getToken(context, "hockeyapp");
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

    // OMG WHY ANDROID !?!?!
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

}
