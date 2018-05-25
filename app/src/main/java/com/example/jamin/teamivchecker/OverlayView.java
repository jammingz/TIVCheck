package com.example.jamin.teamivchecker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class OverlayView extends View {

    private Rect rectangles[][];
    private Paint aboveThresPaint;
    private Paint belowThresPaint;
    private Paint undefinedThreshPaint;
    private Paint perfectPaint;
    private final int rectWidth = 330;
    private final int rectHeight = 391;
    int[][] threshold;
    private final String TAG = "OverlayView";

    public OverlayView(Context context, IntegerPoint[][] grid, int[][] ivThreshold) {
        super(context);

        // DEBUGGING INFO:
        // Width: 330, Height: 391, (29, 29, 300, 361 )
        //    TopLeft: (371,721)
        // BottomRight: (359,721)
        //  TopLeft: (371,721)


        // Convert the grid's reference points into rectangle borders
        if (grid.length == 0 || grid[0].length == 0) {
            Log.d(TAG, "ERROR SIZE IN GRID!");
            return;
        }
        rectangles = new Rect[grid.length][grid[0].length];
        threshold = ivThreshold;

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                IntegerPoint point = grid[i][j];
                int x = point.getX();
                int y = point.getY();
                rectangles[i][j] = new Rect(x,y,x + rectWidth,y + rectHeight);
            }
        }

       // new Rect(371, 721, 371+rectWidth, 721+rectHeight);

        // initialize the color of the rectangles
        aboveThresPaint = new Paint();
        aboveThresPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        aboveThresPaint.setColor(0x90C0EBAE); // Highlighted box converted into hex RBG

        belowThresPaint = new Paint();
        belowThresPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        belowThresPaint.setColor(0x90FFCCCC);


        perfectPaint = new Paint();
        perfectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        perfectPaint.setColor(0x90FFCC00);


        undefinedThreshPaint = new Paint();
        undefinedThreshPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        undefinedThreshPaint.setColor(0x90a6a6a6);

        Log.d("OverlayView", "OverlayView Initialized");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < rectangles.length; i++) {
            for (int j = 0; j < rectangles[0].length; j++) {
                switch (threshold[i][j]) {
                    case 0: canvas.drawRect(rectangles[i][j], undefinedThreshPaint);
                            break;
                    case 1: canvas.drawRect(rectangles[i][j], belowThresPaint);
                            break;
                    case 2: canvas.drawRect(rectangles[i][j], aboveThresPaint);
                            break;
                    case 3: canvas.drawRect(rectangles[i][j], perfectPaint);
                         break;


                }
            }
        }
        Log.d("OverlayView", "onDraw() called");
    }
}
