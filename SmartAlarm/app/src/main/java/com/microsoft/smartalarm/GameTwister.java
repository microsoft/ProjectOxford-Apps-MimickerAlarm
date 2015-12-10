package com.microsoft.smartalarm;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.microsoft.projectoxford.speechrecognition.Confidence;
import com.microsoft.projectoxford.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.projectoxford.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.projectoxford.speechrecognition.RecognitionResult;
import com.microsoft.projectoxford.speechrecognition.RecognitionStatus;
import com.microsoft.projectoxford.speechrecognition.RecognizedPhrase;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionMode;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionServiceFactory;

import java.util.Random;

public class GameTwister extends AppCompatActivity implements ISpeechRecognitionServerEvents {

    private static String LOGTAG = "GameTwister";

    private MicrophoneRecognitionClient mMicClient = null;
    private SpeechRecognitionMode mRecognitionMode;
    private String mUnderstoodText = null;
    private String mQuestion = null;
    private ProgressButton mCaptureButton;
    private CountDownTimerView mTimer;

    private final static int TIMEOUT_MILLISECONDS = 30000;
    private final static float SUCCESS_THRESHOLD = 0.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twister_game);
        AlarmUtils.setLockScreenFlags(getWindow());
        generateQuestion();
        initialize();
        Logger.init(this);
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_TWISTER);
        Logger.track(userAction);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTimer = (CountDownTimerView) findViewById(R.id.countdown_timer);
        mTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AlarmUtils.clearLockScreenFlags(getWindow());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.flush();
    }

    private void generateQuestion() {
        Resources resources = getResources();
        String[] questions = resources.getStringArray(R.array.tongue_twisters);
        mQuestion = questions[new Random().nextInt(questions.length)];

        final TextView instructionTextView = (TextView) findViewById(R.id.instruction_text);
        instructionTextView.setText(mQuestion);
    }

    protected void gameSuccess() {
        mTimer.stop();
        final GameStateBanner stateBanner = (GameStateBanner) findViewById(R.id.game_state);
        String successMessage = getString(R.string.game_success_message);
        stateBanner.success(successMessage, new GameStateBanner.Command() {
            @Override
            public void execute() {
                Intent intent = GameTwister.this.getIntent();
                GameTwister.this.setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
    protected void gameFailure(boolean allowRetry) {
        final GameStateBanner stateBanner = (GameStateBanner) findViewById(R.id.game_state);
        if (allowRetry) {
            String failureMessage = getString(R.string.game_failure_message);
            stateBanner.failure(failureMessage, new GameStateBanner.Command() {
                @Override
                public void execute() {
                    mCaptureButton.readyAudio();
                }
            });
        }
        else {
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_TWISTER_TIMEOUT);
            userAction.putProp(Loggable.Key.PROP_QUESTION, mQuestion);
            Logger.track(userAction);
            String failureMessage = getString(R.string.game_time_up_message);
            stateBanner.failure(failureMessage, new GameStateBanner.Command() {
                @Override
                public void execute() {
                    Intent intent = GameTwister.this.getIntent();
                    GameTwister.this.setResult(RESULT_CANCELED, intent);
                    finish();
                }
            });
        }
    }

    @Override
    public void onPartialResponseReceived(String s) {
        Log.d(LOGTAG, s);
        mUnderstoodText = s;
        TextView understoodText = (TextView) findViewById(R.id.understood_text);
        understoodText.setText(s);
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult response) {
        boolean isFinalDictationMessage = mRecognitionMode == SpeechRecognitionMode.LongDictation &&
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if (mRecognitionMode == SpeechRecognitionMode.ShortPhrase
                || isFinalDictationMessage) {
            mMicClient.endMicAndRecognition();
            mCaptureButton.readyAudio();
            for (RecognizedPhrase res : response.Results) {
                Log.d(LOGTAG, String.valueOf(res.Confidence));
                Log.d(LOGTAG, String.valueOf(res.DisplayText));

                if(res.Confidence == Confidence.Normal) {
                    mUnderstoodText = res.DisplayText;
                }
                else if(res.Confidence == Confidence.High) {
                    mUnderstoodText = res.DisplayText;
                    break;
                }
            }
            verify();
        }
    }

    @Override
    public void onIntentReceived(final String s) {
        Log.d(LOGTAG, s);
    }

    @Override
    public void onError(int errorCode, final String s) {
        Log.e(LOGTAG, s);
    }

    @Override
    public void onAudioEvent(boolean recording) {
        if (!recording) {
            mMicClient.endMicAndRecognition();
            mCaptureButton.readyAudio();
        }
    }

    private void initialize() {
        mRecognitionMode = SpeechRecognitionMode.ShortPhrase;

        try {
            String language = "en-us";
            String subscriptionKey = Util.getToken(this, "speech");
            if (mMicClient == null) {
                mMicClient = SpeechRecognitionServiceFactory.createMicrophoneClient(this, mRecognitionMode, language, this, subscriptionKey);
            }
        }
        catch(Exception e){
            Log.e(LOGTAG, "Speech client failed to initialize " + e);
            Logger.trackException(e);
        }

        mCaptureButton = (ProgressButton) findViewById(R.id.capture_button);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mCaptureButton.isReady()) {
                    mMicClient.startMicAndRecognition();
                    mCaptureButton.waiting();
                } else {
                    mMicClient.endMicAndRecognition();
                    mCaptureButton.readyAudio();
                }
            }
        });
        mCaptureButton.readyAudio();

        mTimer = (CountDownTimerView) findViewById(R.id.countdown_timer);
        mTimer.init(TIMEOUT_MILLISECONDS, new CountDownTimerView.Command() {
            @Override
            public void execute() {
                gameFailure(false);
            }
        });
    }


    //https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance
    public int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        if (lhs == null && rhs == null) {
            return 0;
        }
        if (lhs == null) {
            return rhs.length();
        }
        if (rhs == null) {
            return lhs.length();
        }

        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost; cost = newcost; newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    private void verify() {
        if (mUnderstoodText == null) {
            gameFailure(true);
            return;
        }

        double distance = (double)levenshteinDistance(mUnderstoodText, mQuestion) / (double)mQuestion.length();

        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_TWISTER_SUCCESS);
        userAction.putProp(Loggable.Key.PROP_QUESTION, mQuestion);
        userAction.putProp(Loggable.Key.PROP_DIFF, distance);

        if (distance <= SUCCESS_THRESHOLD) {
            Logger.track(userAction);
            gameSuccess();
        }
        else {
            userAction.Name = Loggable.Key.ACTION_GAME_TWISTER_FAIL;
            Logger.track(userAction);
            gameFailure(true);
        }
    }
}

