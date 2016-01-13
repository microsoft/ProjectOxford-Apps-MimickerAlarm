package com.microsoft.mimicker.mimics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.mimics.MimicFactory.MimicResultListener;
import com.microsoft.mimicker.ringing.ShareFragment;
import com.microsoft.mimicker.utilities.Loggable;
import com.microsoft.mimicker.utilities.Logger;
import com.microsoft.mimicker.utilities.KeyUtil;
import com.microsoft.projectoxford.speechrecognition.Confidence;
import com.microsoft.projectoxford.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.projectoxford.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.projectoxford.speechrecognition.RecognitionResult;
import com.microsoft.projectoxford.speechrecognition.RecognitionStatus;
import com.microsoft.projectoxford.speechrecognition.RecognizedPhrase;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionMode;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionServiceFactory;

import java.util.Random;

public class MimicTongueTwisterFragment extends Fragment implements ISpeechRecognitionServerEvents {
    private final static int TIMEOUT_MILLISECONDS = 15000;
    private final static float DIFFERENCE_SUCCESS_THRESHOLD = 0.3f;
    private final static float DIFFERENCE_PERFECT_THRESHOLD = 0.1f;
    private static String LOGTAG = "MimicTongueTwisterFragment";
    MimicResultListener mCallback;
    private MicrophoneRecognitionClient mMicClient = null;
    private SpeechRecognitionMode mRecognitionMode;
    private String mUnderstoodText = null;
    private String mQuestion = null;
    private ProgressButton mCaptureButton;
    private CountDownTimerView mTimer;
    private MimicStateBanner mStateBanner;
    private TextView mTextResponse;
    private String mSuccessMessage;
    private Uri mSharableUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tongue_twister_mimic, container, false);
        mTimer = (CountDownTimerView) view.findViewById(R.id.countdown_timer);
        mStateBanner = (MimicStateBanner) view.findViewById(R.id.mimic_state);
        mTextResponse = (TextView) view.findViewById(R.id.understood_text);

        generateQuestion(view);
        initialize(view);

        Logger.init(getContext());
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_TWISTER);
        Logger.track(userAction);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (MimicResultListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMicClient != null) {
            mMicClient.endMicAndRecognition();
        }
        mTimer.stop();
        Logger.flush();
    }

    private void generateQuestion(View view) {
        Resources resources = getResources();
        String[] questions = resources.getStringArray(R.array.tongue_twisters);
        mQuestion = questions[new Random().nextInt(questions.length)];

        final TextView instructionTextView = (TextView) view.findViewById(R.id.instruction_text);
        instructionTextView.setText(mQuestion);
    }

    //
    // Create bitmap for sharing
    //
    private void createSharableBitmap() {
        Bitmap sharableBitmap = Bitmap.createBitmap(getView().getWidth(), getView().getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(sharableBitmap);
        canvas.drawColor(getResources().getColor(R.color.white));

        // Load the view for the sharable. This will be drawn to the bitmap
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.fragment_sharable_tongue_twister, null);

        TextView textView = (TextView)layout.findViewById(R.id.twister_sharable_tongue_twister);
        textView.setText(mQuestion);

        textView = (TextView)layout.findViewById(R.id.twister_sharable_i_said);
        textView.setText(mUnderstoodText);

        textView = (TextView)layout.findViewById(R.id.mimic_twister_share_success);
        textView.setText(mSuccessMessage);

        // Perform the layout using the dimension of the bitmap
        int widthSpec = View.MeasureSpec.makeMeasureSpec(canvas.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(canvas.getHeight(), View.MeasureSpec.EXACTLY);
        layout.measure(widthSpec, heightSpec);
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());

        // Draw the generated view to canvas
        layout.draw(canvas);

        String title = getString(R.string.app_short_name) + ": " + getString(R.string.mimic_twister_name);
        mSharableUri = ShareFragment.saveShareableBitmap(getActivity(), sharableBitmap, title);
    }

    protected void gameSuccess(double difference) {
        if (getActivity() == null) {
            return;
        }

        mTimer.stop();
        mSuccessMessage = getString(R.string.mimic_success_message);
        if (difference <= DIFFERENCE_PERFECT_THRESHOLD) {
            mSuccessMessage = getString(R.string.mimic_twister_perfect_message);
        }

        createSharableBitmap();
        mStateBanner.success(mSuccessMessage, new MimicStateBanner.Command() {
            @Override
            public void execute() {
                mCallback.onMimicSuccess(mSharableUri.getPath());
            }
        });
    }

    protected void gameFailure(boolean allowRetry) {
        if (getActivity() == null) {
            return;
        }

        if (allowRetry) {
            mTimer.pause();
            String failureMessage = getString(R.string.mimic_failure_message);
            mStateBanner.failure(failureMessage, new MimicStateBanner.Command() {
                @Override
                public void execute() {
                    mTimer.resume();
                    mCaptureButton.readyAudio();
                }
            });
        }
        else {
            mCaptureButton.setClickable(false);
            Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_TWISTER_TIMEOUT);
            userAction.putProp(Loggable.Key.PROP_QUESTION, mQuestion);
            Logger.track(userAction);
            String failureMessage = getString(R.string.mimic_time_up_message);
            mStateBanner.failure(failureMessage, new MimicStateBanner.Command() {
                @Override
                public void execute() {
                    mCallback.onMimicFailure();
                }
            });
        }
    }

    @Override
    public void onPartialResponseReceived(String s) {
        Log.d(LOGTAG, s);
        mTextResponse.setText(s);
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult response) {
        boolean isFinalDictationMessage = mRecognitionMode == SpeechRecognitionMode.LongDictation &&
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if (mRecognitionMode == SpeechRecognitionMode.ShortPhrase
                || isFinalDictationMessage) {
            mMicClient.endMicAndRecognition();
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

            mTextResponse.setText(mUnderstoodText);
            verify();
        }
    }

    @Override
    public void onIntentReceived(final String s) {
        Log.d(LOGTAG, s);
    }

    @Override
    public void onError(int errorCode, final String s) {
        Loggable.AppError error = new Loggable.AppError(Loggable.Key.APP_ERROR, s);
        Logger.track(error);
    }

    @Override
    public void onAudioEvent(boolean recording) {
        if (!recording) {
            mMicClient.endMicAndRecognition();
        }
    }

    private void initialize(View view) {
        mRecognitionMode = SpeechRecognitionMode.ShortPhrase;

        try {
            //TODO: localize
            String language = "en-us";
            String subscriptionKey = KeyUtil.getToken(getActivity(), "speech");
            if (mMicClient == null) {
                mMicClient = SpeechRecognitionServiceFactory.createMicrophoneClient(getActivity(), mRecognitionMode, language, this, subscriptionKey);
            }
        }
        catch(Exception e){
            Logger.trackException(e);
        }


        mCaptureButton = (ProgressButton) view.findViewById(R.id.capture_button);

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mCaptureButton.isReady()) {
                    mMicClient.startMicAndRecognition();
                    mCaptureButton.waiting();
                } else {
                    mMicClient.endMicAndRecognition();
                }
            }
        });
        mCaptureButton.readyAudio();

        mTimer = (CountDownTimerView) view.findViewById(R.id.countdown_timer);
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

        double difference = (double)levenshteinDistance(mUnderstoodText, mQuestion) / (double)mQuestion.length();

        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_TWISTER_SUCCESS);
        userAction.putProp(Loggable.Key.PROP_QUESTION, mQuestion);
        userAction.putProp(Loggable.Key.PROP_DIFF, difference);

        if (difference <= DIFFERENCE_SUCCESS_THRESHOLD) {
            Logger.track(userAction);
            gameSuccess(difference);
        }
        else {
            userAction.Name = Loggable.Key.ACTION_GAME_TWISTER_FAIL;
            Logger.track(userAction);
            gameFailure(true);
        }
    }
}

