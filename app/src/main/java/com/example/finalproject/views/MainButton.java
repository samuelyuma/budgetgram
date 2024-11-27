package com.example.finalproject.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.finalproject.R;

public class MainButton extends ConstraintLayout {
    private Button button;

    public MainButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.main_button, this, true);

        button = findViewById(R.id.mainButton);

        if (attrs != null) {
            String text = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");
            if (text != null) {
                button.setText(text);
            }
        }
    }

    public void setText(String text) {
        button.setText(text);
    }

    public String getText() {
        return button.getText().toString();
    }
}
