package com.microsoft.mimicker.mimics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.microsoft.mimicker.utilities.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that creates a camera preview for either the front or back camera
 * since the image captured is not really for artistic purposes or for keeping,
 * this camera values speed over quality. It chooses a decent resolution, not the max, and
 * upon capture, immediately returns the last preview frame displayed as opposed to the actual
 * image captured by the camera
 *
 * To use this, pass in
 * a ImageCallback to process the image returned,
 * an aspect ratio to use. The class will find the camera setting that best fits this aspect ratio,
 * a camera facing (Front or Back)
 *
 * Public methods:
 * initPreview (initialize the camera and the preview surface),
 * start (call initPreview before),
 * stop,
 * onCapture (set the ImageCallback),
 * onFocus (focus the camera at a certain x, y position)
 */
@SuppressWarnings("deprecation")
public class CameraPreview implements SurfaceHolder.Callback {
    private static final String LOGTAG = "CameraPreview";
    private static final int MAX_SIZE = 1080;
    private static final double ASPECT_RATIO_EPSILON = 0.02;
    private SurfaceView mPreviewView;
    private Camera mCamera;
    private int mCameraFacing;
    private int mCameraRotation;
    private double mCameraAspectRatio;
    private ImageCallback mCallbackOnCaptured;
    private Camera.PreviewCallback mCaptureCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            camera.stopPreview();
            new processCaptureImage().execute(data, camera);
        }
    };

    public CameraPreview(SurfaceView surfaceView, double aspectRatio, int facing) {
        mCameraAspectRatio = aspectRatio;
        mCameraFacing = facing;
        mPreviewView = surfaceView;
        mCameraRotation = 0;
        final SurfaceHolder surfaceHolder = mPreviewView.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        (new OpenCameraTask()).execute();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    public void initPreview() {
        if (mCamera == null) {
            mCamera = getNewCamera();
        }
        try {
            mCamera.setPreviewDisplay(mPreviewView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
            Logger.trackException(e);
        }
    }

    public void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
    public void start() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    public void onCapture(ImageCallback callback) {
        mCallbackOnCaptured = callback;
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(mCaptureCallback);
        }
    }

    public void onFocus(int x, int y) {
        RectF focusRectF = new RectF(x - 10, y - 10, x + 10, y + 10);
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.postRotate(-mCameraRotation);
        rotateMatrix.mapRect(focusRectF);
        Rect focusRect = new Rect();
        focusRectF.round(focusRect);
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 1000));
            parameters.setFocusAreas(focusAreas);
        }

        try {
            mCamera.cancelAutoFocus();
            mCamera.setParameters(parameters);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {}
            });
        } catch (Exception e) {
            e.printStackTrace();
            Logger.trackException(e);
        }
    }

    private Camera getNewCamera() {
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();

        // Find the camera that's facing the right way
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == mCameraFacing) {
                try {
                    cam = Camera.open(i);
                    break;
                } catch (RuntimeException ex) {
                    Log.e(LOGTAG, "err opening camera", ex);
                    Logger.trackException(ex);
                }
            }
        }

        // Configure the camera with right resolution, aspect ratio and focus
        if (cam != null) {
            try {
                Camera.Parameters params = cam.getParameters();

                // find a camera configuration of the same size as the phone screen.
                List<Camera.Size> supportedSizes = params.getSupportedPreviewSizes();
                Camera.Size bestSize = supportedSizes.get(0);
                for (Camera.Size size : supportedSizes) {
                    if (size.width > MAX_SIZE &&
                            size.height > MAX_SIZE) {
                        continue;
                    }
                    if (Math.abs(((double) size.width / (double) size.height) - mCameraAspectRatio) < ASPECT_RATIO_EPSILON) {
                        if (size.width >= bestSize.width)
                            bestSize = size;
                    }
                }
                params.setPreviewSize(bestSize.width, bestSize.height);

                // if available set the autofocus on
                List<String> supportedFocusModes = params.getSupportedFocusModes();
                for (String mode : supportedFocusModes) {
                    if (mode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        break;
                    }
                }

                mCameraRotation = cameraInfo.orientation;

                // compensate for the front camera mirror
                if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCameraRotation = (360 - mCameraRotation) % 360;
                }
                params.setRotation(mCameraRotation);

                cam.setParameters(params);
                cam.setDisplayOrientation(mCameraRotation);

            } catch (RuntimeException ex) {
                Log.e(LOGTAG, "err configuring camera", ex);
                Logger.trackException(ex);
            }
        }

        return cam;
    }

    public interface ImageCallback {
        void run(Bitmap bitmap);
    }

    private class OpenCameraTask extends AsyncTask<Object, String, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            if (mCamera != null) {
                try {
                    initPreview();
                } catch (Exception ex) {
                    Log.e(LOGTAG, "err starting camera preview", ex);
                    Logger.trackException(ex);
                }
            }
            return null;
        }
    }

    private class processCaptureImage extends AsyncTask<Object, String, Boolean> {
        @Override
        // Decode the image data and rotate it to the proper orientation.
        // then run the callback, if any, on the image to do post processing
        protected Boolean doInBackground(Object... params) {
            byte[] data = (byte[]) params[0];
            Camera camera = (Camera) params[1];
            Camera.Parameters parameters = camera.getParameters();
            int format = parameters.getPreviewFormat();
            //YUV formats require more conversion
            if (format == ImageFormat.NV21 || format == ImageFormat.YUY2 || format == ImageFormat.NV16) {
                int w = parameters.getPreviewSize().width;
                int h = parameters.getPreviewSize().height;
                // Get the YuV image
                YuvImage yuv_image = new YuvImage(data, format, w, h, null);
                // Convert YuV to Jpeg
                Rect rect = new Rect(0, 0, w, h);
                ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
                yuv_image.compressToJpeg(rect, 100, output_stream);
                byte[] imageBytes = output_stream.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                Matrix transform = new Matrix();
                if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    transform.preScale(-1, 1);
                }
                transform.postRotate(mCameraRotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transform, true);

                if (mCallbackOnCaptured != null) {
                    mCallbackOnCaptured.run(bitmap);
                }
            }
            return null;
        }
    }
}

