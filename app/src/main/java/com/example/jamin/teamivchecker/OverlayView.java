package com.example.jamin.teamivchecker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class OverlayView extends View {

    private Rect rectangle;
    private Paint paint;
    private int rectWidth;
    private int rectHeight;

    public OverlayView(Context context) {
        super(context);


        // Width: 330, Height: 391, (29, 29, 300, 361 )
        //    TopLeft: (371,721)

        rectHeight = 391;
        rectWidth = 330;
        rectangle = new Rect(371, 721, 371+rectWidth, 721+rectHeight);

        // initialize the color of the rectangle
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(0x90C0EBAE); // Highlighted box converted into hex RBG
        Log.d("OverlayView", "OverlayView Initialized");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(rectangle, paint);
        Log.d("OverlayView", "onDraw() called");
    }
}
