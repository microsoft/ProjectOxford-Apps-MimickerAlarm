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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.microsoft.mimickeralarm.utilities.Logger;

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
 * a CapturedImageCallbackAsync to process the image returned,
 * an aspect ratio to use. The class will find the camera setting that best fits this aspect ratio,
 * a camera facing (Front or Back)
 *
 * Public methods:
 * initPreview (initialize the camera and the preview surface),
 * start (call initPreview before),
 * stop,
 * onCapture (set the CapturedImageCallbackAsync),
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
    private Boolean mIsFlashSupported; // we only want torch mode. Boolean type so we can cache the result
    private boolean mFlashState;
    private FlashStateCallback mFlashStateCallback;
    private CapturedImageCallbackAsync mCapturedCapturedImageCallbackAsync;
    private CameraInitializedCallback mCameraInitializedCallback;

    private Camera.PreviewCallback mCaptureCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            camera.stopPreview();
            if (mIsFlashSupported) {
                // Delay turning off flash for 0.5s to allow camera to capture image
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changeFlashState(false);
                        if (mFlashStateCallback != null) {
                            mFlashStateCallback.execute(false);
                        }
                    }
                }, 500);
            }
            new processCaptureImage().execute(data, camera);
        }
    };

    public interface CapturedImageCallbackAsync {
        void execute(Bitmap bitmap);
    }

    public interface CameraInitializedCallback {
        void execute(boolean success);
    }

    public interface FlashStateCallback {
        void execute(boolean state);
    }

    private OnCameraPreviewException mOnException;

    public CameraPreview(SurfaceView surfaceView,
                         OnCameraPreviewException onException,
                         CameraInitializedCallback onCameraInitialized,
                         double aspectRatio, int facing) {
        mCameraAspectRatio = aspectRatio;
        mCameraFacing = facing;
        mPreviewView = surfaceView;
        mCameraRotation = 0;
        mIsFlashSupported = null;
        mFlashState = false;
        final SurfaceHolder surfaceHolder = mPreviewView.getHolder();
        surfaceHolder.addCallback(this);
        mOnException = onException;
        mCameraInitializedCallback = onCameraInitialized;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        (new OpenCameraTask()).execute();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    private void initPreview(){
        if (mCamera == null) {
            mCamera = getNewCamera();
        }
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(mPreviewView.getHolder());
            }
        } catch (IOException e) {
            Logger.trackException(e);
        }
    }

    public void stop() {
        if (mCamera != null) {
            changeFlashState(false);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
    public void start() throws MimicException{
        if (mCamera != null) {
            mCamera.startPreview();
        }
        else{
            MimicException e = new MimicException("failed to open camera");
            Logger.trackException(e);
            throw e;
        }
    }

    public void onCapture(CapturedImageCallbackAsync callback, FlashStateCallback flashCallback) {
        mCapturedCapturedImageCallbackAsync = callback;
        mFlashStateCallback = flashCallback;
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
        if (mCamera == null){
            return;
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters == null)
            {
                return;
            }
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(focusAreas);

                mCamera.cancelAutoFocus();
                mCamera.setParameters(parameters);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                    }
                });
            }
        } catch (Exception e) {
            Logger.trackException(e);
        }
    }

    public boolean isFlashSupported() {
        if (mIsFlashSupported != null) {
            return mIsFlashSupported;
        }
        // use mIsFlashSupported to cache;
        mIsFlashSupported = false;

        // Currently let's limit flash to back camera only
        if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return false;
        }

        if (mCamera == null) {
            return false;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return false;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && !flashModes.isEmpty()){
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                mIsFlashSupported = true;
            }
        }

        return mIsFlashSupported;
    }

    public boolean changeFlashState(boolean turnOn) {
        if (mFlashState == turnOn) {
            return true;
        }

        if (mCamera == null) {
            return false;
        }

        if (!isFlashSupported()) {
            return false;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return false;
        }

        try {
            if (turnOn) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            mCamera.setParameters(parameters);
        }
        catch (Exception e) {
            Logger.trackException(e);
            return false;
        }

        mFlashState = turnOn;
        return true;
    }

    public boolean getFlashState() {
        return mFlashState;
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

    private class OpenCameraTask extends AsyncTask<Object, String, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            if(mCamera == null) {
                initPreview();
            }

            boolean success = false;
            try {
                start();
                success = true;
            }
            catch (MimicException ex) {
                Logger.trackException(ex);
            }
            catch (Exception ex) {
                Log.e(LOGTAG, "err starting camera preview", ex);
                Logger.trackException(ex);
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                if (mOnException != null) {
                    mOnException.execute();
                }
            }
            else {
                if (mCameraInitializedCallback != null) {
                    mCameraInitializedCallback.execute(true);
                }
            }
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

                if (mCapturedCapturedImageCallbackAsync != null) {
                    mCapturedCapturedImageCallbackAsync.execute(bitmap);
                }
            }
            return null;
        }
    }

    public interface OnCameraPreviewException{
        void execute();
    }
}

