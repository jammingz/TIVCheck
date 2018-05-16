package com.example.jamin.teamivchecker;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    // Variables for Tesseract-OCR

    private TessBaseAPI tessBaseApi;
    public static final String lang = "eng";
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TeamIVChecker/";


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



        // Check to see if SD card has trained data. If not, it will export trained data into SD card.
        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v("Main", "ERROR: Creation of directory " + path + " on sdcard failed");
                    break;
                } else {
                    Log.v("Main", "Created directory " + path + " on sdcard");
                }
            }

        }
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();

                InputStream in = assetManager.open(lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.d("MainButtonService", "Copied " + lang + " traineddata");
            } catch (IOException e) {
                 Log.d("MainButtonService", "Was unable to copy " + lang + " traineddata " + e.toString());
            }

        }

        // Load train data from SD card


        tessBaseApi = new TessBaseAPI(); // AssetManager assetManager=
        String datapath = Environment.getExternalStorageDirectory() + "/TeamIVChecker/";
        String language = "eng";
        // AssetManager assetManager = getAssets();
        File dir = new File(datapath + "/tessdata/");
        if (!dir.exists())
            dir.mkdirs();
        tessBaseApi.init(datapath, language);


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


        Bitmap cropped = editor.cropImage(gridReferencePoints, 1, 1);
        Bitmap name = editor.cropName(gridReferencePoints, 1, 1);
        // MediaStore.Images.Media.insertImage(getContentResolver(), cropped, "LMAO" , "test");


        if (tessBaseApi != null && cropped != null && name != null) {
            tessBaseApi.setImage(cropped);
            String results = tessBaseApi.getUTF8Text();

            if (results.length() > 2) {
                Log.d("TESSERACT-OCR", "CP: " + results.substring(2));
            } else {
                Log.d("TESSERACT-OCR", "Invalid CP!");

            }


            tessBaseApi.setImage(name);
            results = tessBaseApi.getUTF8Text();


            Log.d("TESSERACT-OCR", "Name: " + results);
            //  return Integer.valueOf(results); // need to cut off CP prefix first

        }

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
