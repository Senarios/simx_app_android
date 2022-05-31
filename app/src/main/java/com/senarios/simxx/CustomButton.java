package com.senarios.simxx;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class CustomButton extends androidx.appcompat.widget.AppCompatButton {
    public CustomButton(Context context) {
        super(context);
        initView(context);
    }

    public void initView(Context context) {

    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomButton);
        String a = array.getString(R.styleable.CustomButton_text);
        setText(a);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomButton, defStyleAttr, 0);
        String a = array.getString(R.styleable.CustomButton_text);
        setText(a);
    }


}
