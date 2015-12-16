package com.microsoft.smartalarm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
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

import com.microsoft.smartalarm.GameFactory.GameResultListener;

@SuppressWarnings("deprecation")
abstract class GameWithCameraFragment extends Fragment {

    GameResultListener mCallback;

    private static final String LOGTAG = "GameWithCameraFragment";
    private static final int TIMEOUT_MILLISECONDS = 30000;
    protected static int CameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private CameraPreview   mCameraPreview;
    private ProgressButton  mCaptureButton;
    private CountDownTimerView      mTimer;
    private GameStateBanner mStateBanner;

    private Point mSize;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_game, container, false);

        mStateBanner = (GameStateBanner) view.findViewById(R.id.game_state);
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
                    int deltaX = (int)(((float)mSize.x - event.getX()) / mSize.x * -2000) + 1000;
                    int deltaY = (int)(((float)mSize.y - event.getY()) / mSize.y * -2000) + 1000;
                    mCameraPreview.onFocus(deltaX, deltaY);
                }
                return true;
            }
        });

        mCaptureButton = (ProgressButton) view.findViewById(R.id.capture_button);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimer.pause();
                mCaptureButton.loading();
                mCameraPreview.onCapture(onCaptureCallback);
            }
        });
        mCaptureButton.readyCamera();

        mTimer = (CountDownTimerView) view.findViewById(R.id.countdown_timer);
        mTimer.init(TIMEOUT_MILLISECONDS, new CountDownTimerView.Command() {
            @Override
            public void execute() {
                gameFailure(false);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (GameResultListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            mCameraPreview.initPreview();
            mCameraPreview.start();
        } catch (Exception ex) {
            Log.e(LOGTAG, "err onResume", ex);
            Logger.trackException(ex);
        }

        mTimer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraPreview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCameraPreview.stop();
        mCaptureButton.stop();
        Logger.flush();
    }

    private CameraPreview.ImageCallback onCaptureCallback = new CameraPreview.ImageCallback() {
        @Override
        public void run(Bitmap bitmap) {
            new processOnProjectOxfordAsync().execute(bitmap);
        }
    };

    public class processOnProjectOxfordAsync extends AsyncTask<Bitmap, String, Uri> {

        @Override
        protected Uri doInBackground(Bitmap... bitmaps) {
            try{
                if (bitmaps.length > 0) {
                    if (verify(bitmaps[0])) {
                        Uri tempFile = GameFactory.saveShareableBitmap(getActivity(), bitmaps[0]);
                        bitmaps[0].recycle();
                        return tempFile;
                    }
                }
            }
            catch (Exception ex) {
                Logger.trackException(ex);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Uri shareableUri) {
            super.onPostExecute(shareableUri);
            mCaptureButton.stop();
            if (shareableUri != null) {
                gameSuccess(shareableUri);
            }
            else{
                gameFailure(true);
            }
        }
    }

    protected void gameSuccess(final Uri shareableUri) {
        mTimer.stop();
        String successMessage = getString(R.string.game_success_message);
        mStateBanner.success(successMessage, new GameStateBanner.Command() {
            @Override
            public void execute() {
                mCallback.onGameSuccess();
            }
        });
    }
    protected void gameFailure(boolean allowRetry) {
        if (allowRetry) {
            mCameraPreview.start();
            mCaptureButton.readyCamera();
            String failureMessage = getString(R.string.game_failure_message);
            mStateBanner.failure(failureMessage, new GameStateBanner.Command() {
                @Override
                public void execute() {
                    mTimer.resume();
                }
            });
        }
        else {
            String failureMessage = getString(R.string.game_time_up_message);
            mStateBanner.failure(failureMessage, new GameStateBanner.Command() {
                @Override
                public void execute() {
                    mCallback.onGameFailure();
                }
            });
        }
    }

    abstract protected Boolean verify(Bitmap bitmap);
}

