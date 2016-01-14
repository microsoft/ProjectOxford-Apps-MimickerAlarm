package com.microsoft.mimicker.mimics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.mimics.MimicFactory.MimicResultListener;
import com.microsoft.mimicker.ringing.ShareFragment;
import com.microsoft.mimicker.utilities.Logger;

@SuppressWarnings("deprecation")
abstract class MimicWithCameraFragment extends Fragment
    implements IMimicImplementation {

    private static final String LOGTAG = "MimicWithCameraFragment";
    private static final int TIMEOUT_MILLISECONDS = 30000;
    // Max width for sending to Project Oxford, reduce latency
    private static final int MAX_WIDTH = 500;
    private static final int LIGHT_THRESHOLD = 50;

    protected static int CameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    MimicResultListener mCallback;
    private CameraPreview   mCameraPreview;
    private MimicCoordinator mCoordinator;
    private Uri mSharableUri;
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private SensorEventListener mLightSensorListener;
    private Toast mTooDarkToast;

    private Point mSize;
    private CameraPreview.ImageCallback onCaptureCallback = new CameraPreview.ImageCallback() {
        @Override
        public void run(Bitmap bitmap) {
            new processOnProjectOxfordAsync().execute(bitmap);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_mimic, container, false);

        SurfaceView previewView = (SurfaceView) view.findViewById(R.id.camera_preview_view);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mSize = size;
        double aspectRatio = size.y > size.x ?
                (double)size.y / (double)size.x : (double)size.x / (double)size.y;
        mCameraPreview = new CameraPreview(previewView, aspectRatio, CameraFacing);

        View overlay = view.findViewById(R.id.camera_preview_overlay);
        overlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Camera sensor ranges from -1000 to 1000 regardless of aspect ratio, sizes, resolution, ...
                    int deltaX = (int) (((float) mSize.x - event.getX()) / mSize.x * -2000) + 1000;
                    int deltaY = (int) (((float) mSize.y - event.getY()) / mSize.y * -2000) + 1000;
                    mCameraPreview.onFocus(deltaX, deltaY);
                }
                return true;
            }
        });

        ProgressButton progressButton = (ProgressButton) view.findViewById(R.id.capture_button);
        progressButton.setReadyState(ProgressButton.State.ReadyCamera);

        mCoordinator = new MimicCoordinator();
        mCoordinator.registerCountDownTimer(
                (CountDownTimerView) view.findViewById(R.id.countdown_timer), TIMEOUT_MILLISECONDS);
        mCoordinator.registerStateBanner((MimicStateBanner) view.findViewById(R.id.mimic_state));
        mCoordinator.registerProgressButton(progressButton, MimicButtonBehavior.CAMERA);
        mCoordinator.registerMimic(this);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        mLightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.values[0] < LIGHT_THRESHOLD) {
                    mTooDarkToast.show();
                }
                else {
                    mTooDarkToast.cancel();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
        // This toast is only shown when there is not enough light
        mTooDarkToast = Toast.makeText(getActivity(), getString(R.string.mimic_camera_too_dark), Toast.LENGTH_SHORT);

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
    public void onStart() {
        super.onStart();

        if (mSensorManager != null && mLightSensorListener != null) {
            mSensorManager.registerListener(mLightSensorListener, mLightSensor, SensorManager.SENSOR_DELAY_UI);
        }

        mCoordinator.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCoordinator.stop();

        mTooDarkToast.cancel();
        if (mSensorManager != null && mLightSensorListener != null) {
            mSensorManager.unregisterListener(mLightSensorListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.flush();
    }

    @Override
    public void initializeCapture() {
        try {
            mCameraPreview.initPreview();
            mCameraPreview.start();
        } catch (Exception ex) {
            Log.e(LOGTAG, "err onResume", ex);
            Logger.trackException(ex);
        }
    }

    @Override
    public void startCapture() {
        mCameraPreview.onCapture(onCaptureCallback);
    }

    @Override
    public void stopCapture() {
        mCameraPreview.stop();
    }

    @Override
    public void onCountDownTimerExpired() {
        gameFailure(null, false);
    }

    @Override
    public void onSucceeded() {
        if (mCallback != null) {
            mCallback.onMimicSuccess(mSharableUri.getPath());
        }
    }

    @Override
    public void onFailed() {
        if (mCallback != null) {
            mCallback.onMimicFailure();
        }
    }

    protected void gameSuccess(final GameResult gameResult) {
        mSharableUri = gameResult.shareableUri;
        String successMessage = getString(R.string.mimic_success_message);
        if (gameResult.message != null) {
            successMessage = gameResult.message;
        }
        mCoordinator.onMimicSuccess(successMessage);
    }

    protected void gameFailure(GameResult gameResult, boolean allowRetry) {
        if (allowRetry) {
            mCameraPreview.start();
            String failureMessage = getString(R.string.mimic_failure_message);
            if (gameResult != null && gameResult.message != null) {
                failureMessage = gameResult.message;
            }
            mCoordinator.onMimicFailureWithRetry(failureMessage);
        } else {
            mCoordinator.onMimicFailure(getString(R.string.mimic_time_up_message));
        }
    }

    abstract protected GameResult verify(Bitmap bitmap);

    public class processOnProjectOxfordAsync extends AsyncTask<Bitmap, String, GameResult> {

        @Override
        protected GameResult doInBackground(Bitmap... bitmaps) {
            GameResult gameResult = null;
            try {
                if (bitmaps.length > 0) {
                    int width = bitmaps[0].getWidth();
                    int height = bitmaps[0].getHeight();
                    float ratio = (float) height / width;
                    width = Math.min(width, MAX_WIDTH);
                    height = (int) (width * ratio);
                    gameResult = verify(Bitmap.createScaledBitmap(bitmaps[0], width, height, true));
                    if (gameResult.success) {
                        gameResult.shareableUri = ShareFragment.saveShareableBitmap(getActivity(), bitmaps[0], gameResult.question);
                        bitmaps[0].recycle();
                    }
                }
            } catch (Exception ex) {
                Logger.trackException(ex);
            }
            return gameResult;
        }


        @Override
        protected void onPostExecute(GameResult gameResult) {
            super.onPostExecute(gameResult);
            if (!mCoordinator.hasStopped()) {
                if (gameResult.success) {
                    gameSuccess(gameResult);
                } else {
                    gameFailure(gameResult, true);
                }
            }
        }
    }

    protected class GameResult {
        boolean success = false;
        String message = null;
        Uri shareableUri = null;
        String question = null;
    }
}

