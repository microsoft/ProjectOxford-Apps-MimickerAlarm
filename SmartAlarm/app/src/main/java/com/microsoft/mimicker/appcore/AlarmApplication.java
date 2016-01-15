package com.microsoft.mimicker.appcore;

import android.app.Application;
import android.content.Context;

import com.uservoice.uservoicesdk.UserVoice;

/**
 * Specialization of the Application class to enable:
 *      Application context access from non-Android framework classes
 *      Initialization of the UserVoice object
 */
public class AlarmApplication extends Application {
    private static Context sContext;
    private static final String MICROSOFT_GARAGE_USER_VOICE_SITE =  "microsoftgarage.uservoice.com";
    private static final int USER_VOICE_MIMICKER_FORUM_ID = 336969;

    public static Context getAppContext() {
        return AlarmApplication.sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AlarmApplication.sContext = getApplicationContext();
        initUserVoiceIntegration();
    }

    //
    // Init uservoice.com user forum integration
    //
    private void initUserVoiceIntegration() {
        com.uservoice.uservoicesdk.Config config = new com.uservoice.uservoicesdk.Config(MICROSOFT_GARAGE_USER_VOICE_SITE);
        config.setForumId(USER_VOICE_MIMICKER_FORUM_ID);
        UserVoice.init(config, this);
    }
}
