package com.microsoft.smartalarm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util {
    public static String getToken(Context caller, String resource) {
        String token = null;
        try {
            ApplicationInfo ai = caller.getPackageManager().getApplicationInfo(caller.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            token = bundle.getString("com.microsoft.smartalarm.token." + resource);
        }
        catch (Exception ex) {
            Logger.trackException(ex);
        }
        return token;
    }

}
