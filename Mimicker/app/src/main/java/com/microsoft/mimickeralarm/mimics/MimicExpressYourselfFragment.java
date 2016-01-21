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

package com.microsoft.mimickeralarm.mimics;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.mimickeralarm.R;
import com.microsoft.mimickeralarm.utilities.Loggable;
import com.microsoft.mimickeralarm.utilities.Logger;
import com.microsoft.mimickeralarm.utilities.KeyUtilities;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

/**
 * Implements the logic and UI of the Express yourself game
 *
 * on start, choose a random emotion (defined in emotion_questions.xml)
 * after capturing an image from the front camera, send it to Project Oxford Emotion API which
 * return a list of detected faces and list of emotions and their probabilities.
 *
 * The game predefined an acceptance rating for each emotion. If the returned emotion has a probability
 * higher than that acceptance rating then the game passes.
 */
public class MimicExpressYourselfFragment extends MimicWithCameraFragment {
    private double mEmotionAcceptance = 0.5;
    private EmotionServiceRestClient mEmotionServiceRestClient;
    private String                  mEmotion;

    @SuppressWarnings("deprecation")
    public MimicExpressYourselfFragment() {
        CameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Resources resources = getResources();

        String subscriptionKey = KeyUtilities.getToken(getActivity(), "emotion");
        mEmotionServiceRestClient = new EmotionServiceRestClient(subscriptionKey);

        String[] emotions = resources.getStringArray(R.array.emotions);
        int randomNumber = new Random().nextInt(emotions.length);
        mEmotion = emotions[randomNumber];
        TextView instruction = (TextView) view.findViewById(R.id.instruction_text);
        int adjectiveId = resources.getIdentifier("emotion_" + mEmotion, "string", getActivity().getPackageName());
        String adjective = resources.getString(adjectiveId);
        instruction.setText(String.format(resources.getString(R.string.mimic_emotion_prompt), adjective));

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
                    if (dominantEmotion.equalsIgnoreCase(mEmotion)) {
                        gameResult.message = String.format(resources.getString(R.string.mimic_emotion_failure_not_enough), adjective);
                    }
                    else {
                        gameResult.message = String.format(resources.getString(R.string.mimic_emotion_failure), adjective);
                    }
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