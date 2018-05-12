package com.example.jamin.teamivchecker;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainButtonService extends Service implements ScreenshotDetectionDelegate.ScreenshotDetectionListener{
    private WindowManager windowManager;
    private View mOverlayView;
    private ImageView mainButton;
    private GestureDetector gestureDetector;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private boolean isOverlayOn;
    final static String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009;

    private ScreenshotDetectionDelegate screenshotDetectionDelegate = new ScreenshotDetectionDelegate(this, this);

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }


    @Override public void onCreate() {
        super.onCreate();

        isOverlayOn = false;

        Log.d("MainButtonService", "onCreate() called");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        // Setting up overlay
        /*
        mOverlayView = new View(this);
        final WindowManager.LayoutParams paramsOverlay = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
        );

        // mOverlayView.setBackgroundColor(0x90000000);
        windowManager.addView(mOverlayView, paramsOverlay);
        */




        /*
        mOverlayView = new OverlayView(this);
        windowManager.addView(mOverlayView, paramsOverlay);
        */
        isOverlayOn = false;


        // Setting up navigation button
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


        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        mainButton.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    // single tap
                    Log.d("MainButtonService", "onClick() triggered");

                    /*
                    View rootView = v.getRootView();
                    store(getScreenShot(rootView), "TestSS01.PNG");
                    Toast.makeText(v.getContext(), "Saving Screenshot to TestSS01.PNG",Toast.LENGTH_SHORT).show();

                    */

                    if (isOverlayOn) {
                        windowManager.removeView(mOverlayView);
                        //windowManager.updateViewLayout(mOverlayView, paramsOverlay);
                        isOverlayOn = false;
                    } else {
                        /*
                        mOverlayView = new OverlayView(getApplicationContext());
                        windowManager.addView(mOverlayView, paramsOverlay);
                        //windowManager.updateViewLayout(mOverlayView, paramsOverlay);
                        isOverlayOn = true;
                        */
                    }
                    return true;
                } else {
                    // your code for move and drag
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

                            Log.d("MainButtonService", "onTouch(): Moved location to: (" + String.valueOf(params.x) + "," + String.valueOf(params.y) + ")");
                            return true;
                    }
                    return false;
                }
            }

        });


        screenshotDetectionDelegate.startScreenshotDetection();
    }

    @Override
    public void onScreenCaptured(String path) {
        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        Log.d("onScreenCaptured: ", path);
        ScreenshotEditor editor = new ScreenshotEditor(path, display);
        IntegerPoint[][] gridReferencePoints = editor.constructGrid();


        final WindowManager.LayoutParams paramsOverlay = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
        );

        mOverlayView = new OverlayView(this, gridReferencePoints);
        windowManager.addView(mOverlayView, paramsOverlay);
        isOverlayOn = true;


    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {

    }

    @Override
    public ContentResolver getContentResolver() {
        return super.getContentResolver();
    }


    // https://stackoverflow.com/questions/19538747/how-to-use-both-ontouch-and-onclick-for-an-imagebutton
    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mainButton != null)  {
            windowManager.removeView(mainButton);
        }

        if (isOverlayOn) {
            windowManager.removeView(mOverlayView);
        }

        screenshotDetectionDelegate.stopScreenshotDetection();
    }

}
