package com.example.deep.teleprompter;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Deep on 7/6/2015.
 */
public class MirrorTextview extends TextView {

    public MirrorTextview(Context context) {
        super(context);
    }

    public MirrorTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.translate(getWidth(), 0);
        canvas.scale(-1, 1);
        super.onDraw(canvas);
    }
}
