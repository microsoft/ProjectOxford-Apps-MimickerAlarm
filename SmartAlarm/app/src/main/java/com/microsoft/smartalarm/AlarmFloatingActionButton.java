package com.microsoft.smartalarm;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

public class AlarmFloatingActionButton extends FloatingActionButton {
    private OnVisibilityChangedListener mVisibilityListener;

    public interface OnVisibilityChangedListener {
        void visibilityChanged(int visibility);
    }

    public void setVisibilityListener(OnVisibilityChangedListener listener) {
        mVisibilityListener = listener;
    }

    public AlarmFloatingActionButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this && mVisibilityListener != null) {
            mVisibilityListener.visibilityChanged(visibility);
        }
    }
}

