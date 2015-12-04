package com.microsoft.smartalarm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LearnMoreActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);

        LinearLayout linksHolder = (LinearLayout) findViewById(R.id.learn_more_links);
        for( int i = 0; i < linksHolder.getChildCount(); i++ ) {
            TextView child = (TextView) linksHolder.getChildAt(i);
            if (child != null) {
                child.setMovementMethod(LinkMovementMethod.getInstance());
                stripUnderlines(child);
            }
        }
    }

    // OMG WHY ANDROID !?!?!
    // http://stackoverflow.com/questions/4096851/remove-underline-from-links-in-textview-android
    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
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
}
