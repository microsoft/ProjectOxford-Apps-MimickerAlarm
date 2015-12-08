package com.microsoft.smartalarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

public class GameNoNetwork extends AppCompatActivity {

    private CountDownTimerView mTimer;
    private final static int TIMEOUT_MILLISECONDS = 30000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nonetwork_game);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        Game game = new Game(this);
        game.setLayoutParams(params);
        ((LinearLayout) findViewById(R.id.game_container)).addView(game);
        mTimer = (CountDownTimerView) findViewById(R.id.countdown_timer);
        mTimer.init(TIMEOUT_MILLISECONDS, new CountDownTimerView.Command() {
            @Override
            public void execute() {
                gameFailure();
            }
        });

        ((TextView) findViewById(R.id.instruction_text)).setText(R.string.game_nonetwork_prompt);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer.start();
    }

    protected void gameFailure() {
        final GameStateBanner stateBanner = (GameStateBanner) findViewById(R.id.game_state);
        //Logger.trackUserAction(Logger.UserAction.GAME_TWISTER_TIMEOUT, null, null);
        String failureMessage = getString(R.string.game_time_up_message);
        stateBanner.failure(failureMessage, new GameStateBanner.Command() {
            @Override
            public void execute() {
                Intent intent = GameNoNetwork.this.getIntent();
                GameNoNetwork.this.setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    protected void gameSuccess() {
        mTimer.stop();
        final GameStateBanner stateBanner = (GameStateBanner) findViewById(R.id.game_state);
        String successMessage = getString(R.string.game_success_message);
        stateBanner.success(successMessage, new GameStateBanner.Command() {
            @Override
            public void execute() {
                Intent intent = GameNoNetwork.this.getIntent();
                GameNoNetwork.this.setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


    private class Game extends SurfaceView implements View.OnLayoutChangeListener, SurfaceHolder.Callback {
        private int mWidth, mHeight;
        private GameLoop mGameLoop;
        private GameEngine mGameEngine;
        private GameNoNetwork mParentActivity;

        public Game(Context context) {
            super(context);
            addOnLayoutChangeListener(this);
            SurfaceHolder holder = getHolder();
            holder.addCallback(this);
            mGameEngine  = new GameEngine();
            mGameLoop = new GameLoop(holder, mGameEngine);
            mParentActivity = (GameNoNetwork) context;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int tapsRemaining = mGameEngine.touch(event);

            if (tapsRemaining == 2) {
                ((TextView) mParentActivity.findViewById(R.id.instruction_text)).setText(R.string.game_nonetwork_prompt2);
            }
            else if (tapsRemaining == 1) {
                ((TextView) mParentActivity.findViewById(R.id.instruction_text)).setText(R.string.game_nonetwork_prompt3);
            }
            else if (tapsRemaining <= 0){
                stopLoop();
                mParentActivity.gameSuccess();
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
                    //TODO: log
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
                    //TODO: Log
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
        private Paint mPaint, mBackgroundPaint;
        private float mX, mY;
        private int mWidth, mHeight;
        private Vector2D mVelocity;
        private final float EPSILON = 0.002f;
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
