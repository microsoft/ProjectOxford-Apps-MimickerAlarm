package com.microsoft.mimicker.appcore;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

public class AlarmFloatingActionButton extends FloatingActionButton {
    private OnVisibilityChangedListener mVisibilityListener;

    public AlarmFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setVisibilityListener(OnVisibilityChangedListener listener) {
        mVisibilityListener = listener;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this && mVisibilityListener != null) {
            mVisibilityListener.visibilityChanged(visibility);
        }
    }

    public interface OnVisibilityChangedListener {
        void visibilityChanged(int visibility);
    }
}

