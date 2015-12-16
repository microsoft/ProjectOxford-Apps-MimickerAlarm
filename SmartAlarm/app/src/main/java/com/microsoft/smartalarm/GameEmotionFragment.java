package com.microsoft.smartalarm;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Random;

public class GameEmotionFragment extends GameWithCameraFragment {
    private static final double EMOTION_ACCEPTANCE = 0.6;
    private static String LOGTAG = "GameEmotionFragment";
    private EmotionServiceRestClient mEmotionServiceRestClient;
    private String                  mEmotion;

    public GameEmotionFragment() {
        CameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Resources resources = getResources();

        String subscriptionKey = Util.getToken(getActivity(), "emotion");
        mEmotionServiceRestClient = new EmotionServiceRestClient(subscriptionKey);

        String[] emotions = resources.getStringArray(R.array.emotions);
        String[] adjectives = resources.getStringArray(R.array.emotions_adjectives);
        int randomNumber = new Random().nextInt(emotions.length);
        mEmotion = emotions[randomNumber];
        TextView instruction = (TextView) view.findViewById(R.id.instruction_text);
        instruction.setText(String.format(resources.getString(R.string.game_emotion_prompt), adjectives[randomNumber]));

        Logger.init(getActivity());
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_EMOTION);
        Logger.track(userAction);

        return view;
    }

    @Override
    public Boolean verify(Bitmap bitmap) {
        try{
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
            List<RecognizeResult> result = mEmotionServiceRestClient.recognizeImage(inputStream);

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

            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_EMOTION_SUCCESS);
            userAction.putProp(Loggable.Key.PROP_QUESTION, mEmotion);
            userAction.putEmotions(result);
            if (success)
            {
                Logger.track(userAction);
                return true;
            }
            else {
                userAction.Name = Loggable.Key.ACTION_GAME_EMOTION_FAIL;
                Logger.track(userAction);
                return false;
            }
        }
        catch(Exception ex) {
            Logger.trackException(ex);
        }

        return false;
    }

    @Override
    protected void gameFailure(boolean allowRetry) {
        if (!allowRetry){
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_EMOTION_TIMEOUT);
            userAction.putProp(Loggable.Key.PROP_QUESTION, mEmotion);
            Logger.track(userAction);
        }
        super.gameFailure(allowRetry);
    }
}


