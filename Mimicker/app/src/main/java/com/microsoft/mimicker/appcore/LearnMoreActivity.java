/*
 *
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license.
 *
 * Project Oxford: http://ProjectOxford.ai
 *
 * Project Oxford Mimicker Alarm Github:
 * https://github.com/Microsoft/ProjectOxford-Apps-MimickerAlarm
 *
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License:
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.microsoft.mimicker.appcore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.utilities.Loggable;
import com.microsoft.mimicker.utilities.Logger;
import com.microsoft.mimicker.utilities.GeneralUtilities;

/**
 * This class hosts the learn more/help experience for the application.  It is invoked from the
 * options menu.
 */
public class LearnMoreActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);

        Toolbar bar = (Toolbar) findViewById(R.id.learn_more_toolbar);
        setSupportActionBar(bar);

        LinearLayout linksHolder = (LinearLayout) findViewById(R.id.learn_more_links);
        for( int i = 0; i < linksHolder.getChildCount(); i++ ) {
            TextView child = (TextView) linksHolder.getChildAt(i);
            GeneralUtilities.enableLinks(child);
        }

        Logger.init(this);
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_LEARN_MORE);
        Logger.track(userAction);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.flush();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
