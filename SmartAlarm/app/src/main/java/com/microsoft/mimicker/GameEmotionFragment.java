package com.microsoft.mimicker;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

public class GameEmotionFragment extends GameWithCameraFragment {
    private double mEmotionAcceptance = 0.5;
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
        int randomNumber = new Random().nextInt(emotions.length);
        mEmotion = emotions[randomNumber];
        TextView instruction = (TextView) view.findViewById(R.id.instruction_text);
        int adjectiveId = resources.getIdentifier("emotion_" + mEmotion, "string", getActivity().getPackageName());
        String adjective = resources.getString(adjectiveId);
        instruction.setText(String.format(resources.getString(R.string.game_emotion_prompt), adjective));

        TypedArray acceptances = resources.obtainTypedArray(R.array.emotion_acceptance);
        mEmotionAcceptance = acceptances.getFloat(randomNumber, 0.5f);
        acceptances.recycle();

        Logger.init(getActivity());
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_EMOTION);
        Logger.track(userAction);

        return view;
    }

    @Override
    public GameResult verify(Bitmap bitmap) {
        GameResult gameResult = new GameResult();
        gameResult.question = ((TextView) getView().findViewById(R.id.instruction_text)).getText().toString();

        try{
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
            Loggable.AppAction appAction = new Loggable.AppAction(Loggable.Key.APP_API_EMOTION);
            Logger.trackDurationStart(appAction);
            List<RecognizeResult> result = mEmotionServiceRestClient.recognizeImage(inputStream);
            Logger.track(appAction);

            String dominantEmotion = null;
            double dominantEmotionScore = 0;
            for (RecognizeResult r : result) {
                for (Field field : r.scores.getClass().getFields()) {
                    double score = (double)field.get(r.scores);
                    if (field.getName().equalsIgnoreCase(mEmotion)) {
                        if (score > mEmotionAcceptance) {
                            gameResult.success = true;
                            break;
                        }
                    }
                    if (score > dominantEmotionScore) {
                        dominantEmotion = field.getName();
                        dominantEmotionScore = score;
                    }
                }
            }

            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_EMOTION_SUCCESS);
            userAction.putProp(Loggable.Key.PROP_QUESTION, mEmotion);
            userAction.putEmotions(result);
            if (gameResult.success)
            {
                Logger.track(userAction);
            }
            else {
                userAction.Name = Loggable.Key.ACTION_GAME_EMOTION_FAIL;
                Logger.track(userAction);
                if (dominantEmotion != null) {
                    Resources resources = getResources();
                    int adjectiveId = resources.getIdentifier("emotion_" + dominantEmotion, "string", getActivity().getPackageName());
                    String adjective = resources.getString(adjectiveId);
                    gameResult.message = String.format(resources.getString(R.string.game_emotion_failure), adjective);
                }
            }
        }
        catch(Exception ex) {
            Logger.trackException(ex);
            gameResult.success = false;
        }

        return gameResult;
    }

    @Override
    protected void gameFailure(GameResult gameResult, boolean allowRetry) {
        if (!allowRetry){
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_EMOTION_TIMEOUT);
            userAction.putProp(Loggable.Key.PROP_QUESTION, mEmotion);
            Logger.track(userAction);
        }
        super.gameFailure(gameResult, allowRetry);
    }
}


