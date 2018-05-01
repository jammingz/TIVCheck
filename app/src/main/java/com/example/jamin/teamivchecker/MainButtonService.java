package com.example.jamin.teamivchecker;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainButtonService extends Service{
    private WindowManager windowManager;
    private ImageView mainButton;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();

        Log.d("MainButtonService", "onCreate() called");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Log.d("MainButtonService", "getSystemService passed");
        mainButton = new ImageView(this);
        mainButton.setImageResource(R.drawable.ic_sentiment_satisfied_black_36dp);
        mainButton.setBackgroundColor(Color.GRAY);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        Log.d("MainButtonService", "mainButton defined");
        windowManager.addView(mainButton, params);

        Log.d("MainButtonService", "windowManager added mainButton");


        mainButton.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(mainButton, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mainButton != null) windowManager.removeView(mainButton);
    }
}
