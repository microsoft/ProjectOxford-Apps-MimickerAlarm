package com.microsoft.smartalarm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

@SuppressWarnings("deprecation")
public abstract class GameCameraBase extends AppCompatActivity{

    private static final String LOGTAG = "GameCameraBase";
    private static final int TIMEOUT_MILLISECONDS = 30000;
    protected static int CameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private CameraPreview   mCameraPreview;
    private ProgressButton  mCaptureButton;

    private Point mSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_game);
        SurfaceView previewView = (SurfaceView) findViewById(R.id.camera_preview_view);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mSize = size;
        double aspectRatio = size.y > size.x ?
                (double)size.y / (double)size.x : (double)size.x / (double)size.y;
        mCameraPreview = new CameraPreview(previewView, aspectRatio, CameraFacing);

        View overlay = findViewById(R.id.camera_preview_overlay);
        overlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int deltaX = (int)(((float)mSize.x - event.getX()) / mSize.x * -2000) + 1000;
                    int deltaY = (int)(((float)mSize.y - event.getY()) / mSize.y * -2000) + 1000;
                    mCameraPreview.onFocus(deltaX, deltaY);
                }
                return true;
            }
        });

        mCaptureButton = (ProgressButton) findViewById(R.id.capture_button);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCaptureButton.loading();
                mCameraPreview.onCapture(onCaptureCallback);
            }
        });

        final CountDownTimerView timer = (CountDownTimerView) findViewById(R.id.countdown_timer);
        timer.init(TIMEOUT_MILLISECONDS, new CountDownTimerView.Command() {
            @Override
            public void execute() {
                gameFailure(false);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mCameraPreview.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mCameraPreview.initPreview();
            mCameraPreview.start();
        } catch (Exception ex) {
            Log.e(LOGTAG, "err onResume", ex);
        }

        final CountDownTimerView timer = (CountDownTimerView) findViewById(R.id.countdown_timer);
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraPreview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCameraPreview.stop();
        mCaptureButton.stop();
    }

    private CameraPreview.ImageCallback onCaptureCallback = new CameraPreview.ImageCallback() {
        @Override
        public void run(Bitmap bitmap) {
            new doOxfordAsync().execute(bitmap);
        }
    };

    public class doOxfordAsync extends AsyncTask<Bitmap, String, Boolean> {
        private Exception ex = null;

        @Override
        protected Boolean doInBackground(Bitmap... bitmaps) {
            try{
                if (bitmaps.length > 0) {
                    if (verify(bitmaps[0])) {
                        return true;
                    }
                }
            }
            catch (Exception ex) {
                this.ex = ex;
                Log.e(LOGTAG, "Error on doInBackground", ex);
            }
            return false;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            mCaptureButton.stop();
            if (success) {
                gameSuccess();
            }
            else{
                gameFailure(true);
            }
        }
    }

    protected void gameSuccess() {
        Intent intent = this.getIntent();
        this.setResult(RESULT_OK, intent);
        finish();
    }
    protected void gameFailure(boolean allowRetry) {
        if (allowRetry) {
            mCameraPreview.start();
            mCaptureButton.ready();
        }
        else {
            Intent intent = this.getIntent();
            this.setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    abstract protected Boolean verify(Bitmap bitmap);
}

