package com.microsoft.smartalarm;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.TextView;

import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalyzeResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

public class GameColorFinderActivity extends GameWithCameraActivity {
    private VisionServiceRestClient mVisionServiceRestClient;
    private String                  mQuestionColorName;
    private int                     mQuestionColorCode;
    private static final int        COLOR_DIFF_ACCEPTANCE = 300;

    public GameColorFinderActivity() {
        CameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resources = getResources();

        String subscriptionKey = Util.getToken(this, "vision");
        mVisionServiceRestClient = new VisionServiceRestClient(subscriptionKey);

        String[] questions = resources.getStringArray(R.array.vision_color_codes);
        String colorCode = questions[new Random().nextInt(questions.length)];
        mQuestionColorCode = rgbToInt(colorCode);
        TextView instruction = (TextView) findViewById(R.id.instruction_text);
        int colorNameId = resources.getIdentifier("_" + colorCode, "string", getPackageName());
        mQuestionColorName = resources.getString(colorNameId);
        instruction.setText(String.format(resources.getString(R.string.game_vision_prompt), mQuestionColorName));

        Logger.init(this);
        Loggable playGameEvent = new Loggable.UserAction(Loggable.Key.ACTION_GAME_COLOR);
        Logger.track(playGameEvent);
    }

    @Override
    public Boolean verify(Bitmap bitmap) {
        try{
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
            String[] features = {"Color"};
            AnalyzeResult result = mVisionServiceRestClient.analyzeImage(inputStream, features);
            bitmap.recycle();

            double colorDistance = colorDistance(mQuestionColorCode, rgbToInt(result.color.accentColor));
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_COLOR_SUCCESS);
            userAction.putProp(Loggable.Key.PROP_QUESTION, mQuestionColorName);
            userAction.putProp(Loggable.Key.PROP_DIFF, colorDistance);

            boolean success = false;
            //TODO: this will not work for languages other than English.
            if (colorDistance < COLOR_DIFF_ACCEPTANCE ||
                    result.color.dominantColorForeground.toLowerCase().equals(mQuestionColorName) ||
                    result.color.dominantColorBackground.toLowerCase().equals(mQuestionColorName))
            {
                success = true;
            }

            for (String color : result.color.dominantColors) {
                if (color.toLowerCase().equals(mQuestionColorName)) {
                    success = true;
                    break;
                }
            }

            if (!success) {
                userAction.Name = Loggable.Key.ACTION_GAME_COLOR_FAIL;
            }

            Logger.track(userAction);
            return success;
        }
        catch(Exception ex) {
        }

        return false;
    }

    @Override
    protected void gameFailure(boolean allowRetry) {
        if (!allowRetry){
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_COLOR_TIMEOUT);
            userAction.putProp(Loggable.Key.PROP_QUESTION, mQuestionColorName);
            Logger.track(userAction);
        }
        super.gameFailure(allowRetry);
    }

    private int rgbToInt(String c) {
        return Integer.parseInt(c, 16);
    }

    private double colorDistance(int c1, int c2)
    {
        long rmean = ( (long)Color.red(c1) + (long)Color.red(c2) ) / 2;
        long r = (long)Color.red(c1) - (long)Color.red(c2);
        long g = (long)Color.green(c1) - (long)Color.green(c2);
        long b = (long)Color.blue(c1) - (long)Color.blue(c2);
        return Math.sqrt((((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8));
    }
}


