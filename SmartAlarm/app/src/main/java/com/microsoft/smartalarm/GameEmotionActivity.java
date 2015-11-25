package com.microsoft.smartalarm;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameEmotionActivity extends GameWithCameraActivity {
    private EmotionServiceRestClient mEmotionServiceRestClient;
    private static String           LOGTAG = "GameEmotionActivity";
    private String                  mEmotion;
    private static final double     EMOTION_ACCEPTANCE = 0.6;

    public GameEmotionActivity() {
        CameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resources = getResources();

        String subscriptionKey = resources.getString(R.string.emotion_service_key);
        mEmotionServiceRestClient = new EmotionServiceRestClient(subscriptionKey);

        String[] emotions = resources.getStringArray(R.array.emotions);
        String[] adjectives = resources.getStringArray(R.array.emotions_adjectives);
        int randomNumber = new Random().nextInt(emotions.length);
        mEmotion = emotions[randomNumber];
        TextView instruction = (TextView) findViewById(R.id.instruction_text);
        instruction.setText(String.format(resources.getString(R.string.game_emotion_prompt), adjectives[randomNumber]));

        Logger.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.trackUserAction(Logger.UserAction.GAME_COLOR, null, null);
    }

    @Override
    public Boolean verify(Bitmap bitmap) {
        try{
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
            List<RecognizeResult> result = mEmotionServiceRestClient.recognizeImage(inputStream);

            bitmap.recycle();

            Map<String, String> properties = new HashMap<>();
            properties.put("match-emotion", mEmotion);

            Boolean success = false;
            for (RecognizeResult r : result) {
                switch(mEmotion) {
                    case "anger":
                        success |= r.scores.anger > EMOTION_ACCEPTANCE;
                        break;
                    case "contempt":
                        success |= r.scores.contempt > EMOTION_ACCEPTANCE;
                        break;
                    case "disgust":
                        success |= r.scores.disgust > EMOTION_ACCEPTANCE;
                        break;
                    case "fear":
                        success |= r.scores.fear > EMOTION_ACCEPTANCE;
                        break;
                    case "happiness":
                        success |= r.scores.happiness > EMOTION_ACCEPTANCE;
                        break;
                    case "neutral":
                        success |= r.scores.neutral > EMOTION_ACCEPTANCE;
                        break;
                    case "sadness":
                        success |= r.scores.sadness > EMOTION_ACCEPTANCE;
                        break;
                    case "surprise":
                        success |= r.scores.surprise > EMOTION_ACCEPTANCE;
                        break;
                }
            }

            if (success)
            {
                Logger.trackUserAction(Logger.UserAction.GAME_EMOTION_SUCCESS, properties, null);
                return true;
            }
            else {
                Logger.trackUserAction(Logger.UserAction.GAME_EMOTION_FAIL, properties, null);
                return false;
            }
        }
        catch(Exception ex) {
            Log.e(LOGTAG, "Error calling ProjectOxford", ex);
            Logger.trackException(ex);
        }

        return false;
    }

    @Override
    protected void gameFailure(boolean allowRetry) {
        Map<String, String> properties = new HashMap<>();
        properties.put("match-emotion", mEmotion);
        if (!allowRetry){
            Logger.trackUserAction(Logger.UserAction.GAME_EMOTION_TIMEOUT, properties, null);
        }
        super.gameFailure(allowRetry);
    }
}


