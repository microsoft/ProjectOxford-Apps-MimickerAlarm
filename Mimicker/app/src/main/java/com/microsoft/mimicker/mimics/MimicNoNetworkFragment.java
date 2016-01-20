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

package com.microsoft.mimicker.mimics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.mimicker.R;
import com.microsoft.mimicker.mimics.MimicFactory.MimicResultListener;
import com.microsoft.mimicker.utilities.Loggable;
import com.microsoft.mimicker.utilities.Logger;

import java.util.Random;

public class MimicNoNetworkFragment extends Fragment {
    private final static int TIMEOUT_MILLISECONDS = 30000;
    MimicResultListener mCallback;
    private CountDownTimerView mTimer;
    private TextView mInstructionText;
    private MimicStateBanner mStateBanner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_network_mimic, container, false);

        mStateBanner = (MimicStateBanner) view.findViewById(R.id.mimic_state);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        Game game = new Game(this);
        game.setLayoutParams(params);
        ((LinearLayout) view.findViewById(R.id.game_container)).addView(game);
        mTimer = (CountDownTimerView) view.findViewById(R.id.countdown_timer);
        mTimer.init(TIMEOUT_MILLISECONDS, new CountDownTimerView.Command() {
            @Override
            public void execute() {
                gameFailure();
            }
        });

        mInstructionText = (TextView) view.findViewById(R.id.instruction_text);
        mInstructionText.setText(R.string.game_nonetwork_prompt);

        Logger.init(getActivity());
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_NONETWORK);
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
    public void onStart() {
        super.onStart();
        mTimer.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mTimer.stop();
    }

    protected void gameFailure() {
        String failureMessage = getString(R.string.mimic_time_up_message);
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_NONETWORK_TIMEOUT);
        Logger.track(userAction);
        mStateBanner.failure(failureMessage, new MimicStateBanner.Command() {
            @Override
            public void execute() {
                mCallback.onMimicFailure();
            }
        });
    }

    protected void gameSuccess() {
        mTimer.stop();
        String successMessage = getString(R.string.mimic_success_message);
        Loggable.UserAction userAction = new Loggable.UserAction(Loggable.Key.ACTION_GAME_NONETWORK_SUCCESS);
        Logger.track(userAction);
        mStateBanner.success(successMessage, new MimicStateBanner.Command() {
            @Override
            public void execute() {
                mCallback.onMimicSuccess(null);
            }
        });
    }


    private class Game extends SurfaceView implements View.OnLayoutChangeListener, SurfaceHolder.Callback {
        private int mWidth, mHeight;
        private GameLoop mGameLoop;
        private GameEngine mGameEngine;
        private MimicNoNetworkFragment mParentFragment;

        public Game(MimicNoNetworkFragment parent) {
            super(getActivity());
            addOnLayoutChangeListener(this);
            SurfaceHolder holder = getHolder();
            holder.addCallback(this);
            mGameEngine  = new GameEngine();
            mGameLoop = new GameLoop(holder, mGameEngine);
            mParentFragment = parent;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int tapsRemaining = mGameEngine.touch(event);

            if (tapsRemaining == 2) {
                mInstructionText.setText(R.string.game_nonetwork_prompt2);
            }
            else if (tapsRemaining == 1) {
                mInstructionText.setText(R.string.game_nonetwork_prompt3);
            }
            else if (tapsRemaining <= 0){
                stopLoop();
                mParentFragment.gameSuccess();
            }

            return super.onTouchEvent(event);
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            mWidth = right - left;
            mHeight = bottom - top;
            mGameEngine.setDimensions(mWidth, mHeight);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mGameLoop.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopLoop();
        }

        private void stopLoop() {
            mGameLoop.setRunning(false);
            boolean retry = true;
            while (retry)
            {
                try
                {
                    mGameLoop.join();
                    retry = false;
                }
                catch (InterruptedException e) {
                    Logger.trackException(e);
                }
            }
        }
    }

    private class GameLoop extends Thread {
        private final SurfaceHolder mSurfaceHolder;
        private final long  DELAY = 4;
        private boolean mRunning;
        private GameEngine mGameEngine;

        public GameLoop(SurfaceHolder surfaceHolder, GameEngine gameEngine) {
            mSurfaceHolder = surfaceHolder;
            mGameEngine = gameEngine;
            mRunning = true;
        }

        @Override
        public void run() {
            while (mRunning)
            {
                mGameEngine.update();
                Canvas canvas = mSurfaceHolder.lockCanvas(null);
                if (canvas != null) {
                    synchronized (mSurfaceHolder) {
                        mGameEngine.draw(canvas);
                    }
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }

                try {
                    Thread.sleep(DELAY);
                }
                catch (InterruptedException ex) {
                    Logger.trackException(ex);
                }
            }
        }

        public boolean IsRunning() {
            return mRunning;
        }

        public void setRunning(boolean state) {
            mRunning = state;
        }
    }

    private class GameEngine {
        private final float EPSILON = 0.002f;
        private Paint mPaint, mBackgroundPaint;
        private float mX, mY;
        private int mWidth, mHeight;
        private Vector2D mVelocity;
        private boolean mInitialized = false;
        private RectF mHitbox;
        private int mTapsRemaining = 3;

        private Bitmap mAsset1;
        private Bitmap mAsset2;

        public GameEngine() {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.GREEN);
            mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBackgroundPaint.setColor(Color.WHITE);
            mX = 0; mY = 0;

            mAsset1 = BitmapFactory.decodeResource(getResources(), R.drawable.offline_game_1);
            mAsset2 = BitmapFactory.decodeResource(getResources(), R.drawable.offline_game_2);
            mHitbox = new RectF(0, 0, mAsset1.getWidth(), mAsset1.getHeight());
        }

        public void setDimensions(int width, int height) {
            if (!mInitialized) {
                mX = width / 2;
                mY = height / 2;
                mWidth = width;
                mHeight = height;
                mVelocity = new Vector2D(new Random().nextFloat(), new Random().nextFloat());
                mVelocity.normalize();
                mVelocity.mult(10f);
                mInitialized = true;
                update();
            }
        }

        public void update() {
            float newX = mX + mVelocity.x();
            float newY = mY + mVelocity.y();

            if (newX < EPSILON) {
                mVelocity.x(-mVelocity.x());
                mX = EPSILON;
            }
            else if (newX > mWidth - mHitbox.width()- EPSILON) {
                mVelocity.x(-mVelocity.x());
                mX = mWidth - mHitbox.width() - EPSILON;
            }
            else {
                mX = newX;
            }

            if (newY < EPSILON) {
                mVelocity.y(-mVelocity.y());
                mY = EPSILON;
            }
            else if (newY > mHeight - mHitbox.height() - EPSILON) {
                mVelocity.y(-mVelocity.y());
                mY = mHeight - mHitbox.height() - EPSILON;
            }
            else {
                mY = newY;
            }
            mHitbox.offsetTo(mX, mY);
        }

        public void draw(Canvas canvas) {
            canvas.drawRect(0, 0, mWidth, mHeight, mBackgroundPaint);
            canvas.save();
            canvas.translate(mX, mY);
            canvas.drawBitmap(mAsset1, 0, 0, mPaint);
            canvas.translate(mAsset1.getWidth(), 0);
            for (int i = 0; i < mTapsRemaining; i++) {
                mPaint.setAlpha((int)(Math.pow(0.8, i) * 255));
                canvas.drawBitmap(mAsset2, 0, 0, mPaint);
                canvas.scale(0.8f, 0.8f);
                canvas.translate(75, -50);
            }
            mPaint.setAlpha(255);
            canvas.restore();
        }

        public int touch(MotionEvent event) {
            if (mHitbox.contains(event.getX(), event.getY())){
                mVelocity.mult(1.5f);
                mTapsRemaining--;
            }
            return mTapsRemaining;
        }
    }

    private class Vector2D {
        private float [] v = new float[2];
        public Vector2D(float x, float y) {
            v[0] = x;
            v[1] = y;
        }
        public float x() {
            return v[0];
        }
        public float y() {
            return v[1];
        }
        public void x(float newX) {
            v[0] = newX;
        }
            public void y(float newY) {
            v[1] = newY;
        }

        public float length() {
            return (float) Math.sqrt(x() * x() +  y() * y());
        }

        public void normalize() {
            float length = length();
            if (length > 0){
                v[0] = v[0] / length;
                v[1] = v[1] / length;
            }
        }

        public void mult(float f) {
            v[0] *= f;
            v[1] *= f;
        }

    }
}
