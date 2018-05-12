package com.example.jamin.teamivchecker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class OverlayView extends View {

    private Rect rectangles[];
    private Paint paint;
    private final int rectWidth = 330;
    private final int rectHeight = 391;

    public OverlayView(Context context, IntegerPoint[][] grid) {
        super(context);

        // DEBUGGING INFO:
        // Width: 330, Height: 391, (29, 29, 300, 361 )
        //    TopLeft: (371,721)
        // BottomRight: (359,721)
        //  TopLeft: (371,721)


        // Convert the grid's reference points into rectangle borders
        rectangles = new Rect[grid.length * grid[0].length];

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                IntegerPoint point = grid[i][j];
                int x = point.getX();
                int y = point.getY();
                rectangles[i * grid.length + j] = new Rect(x,y,x + rectWidth,y + rectHeight);
            }
        }

       // new Rect(371, 721, 371+rectWidth, 721+rectHeight);

        // initialize the color of the rectangle
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(0x90C0EBAE); // Highlighted box converted into hex RBG
        Log.d("OverlayView", "OverlayView Initialized");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < rectangles.length; i++) {
            canvas.drawRect(rectangles[i], paint);
        }
        Log.d("OverlayView", "onDraw() called");
    }
}
