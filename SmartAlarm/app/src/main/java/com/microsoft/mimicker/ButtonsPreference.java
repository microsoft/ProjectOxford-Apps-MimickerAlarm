package com.microsoft.mimicker;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class ButtonsPreference extends Preference {

    private String mLeftButtonText;
    private String mRightButtonText;

    public ButtonsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_buttons);
        setEnabled(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Button leftButton = (Button) holder.findViewById(R.id.left_settings_button);
        Button rightButton = (Button) holder.findViewById(R.id.right_settings_button);
        leftButton.setText(mLeftButtonText);
        rightButton.setText(mRightButtonText);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callChangeListener(false);
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callChangeListener(true);
            }
        });
        leftButton.setEnabled(true);
        rightButton.setEnabled(true);
    }

    public void setLeftButtonText(String buttonText) {
        mLeftButtonText = buttonText;
    }

    public void setRightButtonText(String buttonText) {
        mRightButtonText = buttonText;
    }
}
