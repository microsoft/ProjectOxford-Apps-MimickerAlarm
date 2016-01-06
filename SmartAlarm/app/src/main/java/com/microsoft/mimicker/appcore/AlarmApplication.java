package com.microsoft.mimicker.appcore;

import android.app.Application;
import android.content.Context;

import com.uservoice.uservoicesdk.UserVoice;

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

        //
        // TODO: REMOVE THIS LINE BEFORE SHIP. For testing only because our forum is hidden
        // Task 8007: UserVoice Integration: Remove the yslin user ID in source code for testing user voice integration and remove hockeyapp feedback link
        // Assigned to DelFu
        //
        config.identifyUser("USER_ID", "Yung-Shin", "yslin@microsoft.com");

        UserVoice.init(config, this);
    }
}
