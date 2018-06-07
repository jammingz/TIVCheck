package com.example.jamin.teamivchecker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.ArrayList;


public class MainButtonService extends Service implements ScreenshotDetectionDelegate.ScreenshotDetectionListener{
    private static final String TAG = "MainButtonService";
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

    private TessBaseAPI numTessBaseApi; // for numbers-only

    protected static final int NOTIFICATION_ID = 1;
    private Notification.Builder mNotificationBuilder;
    private static final String SHARED_PREFERENCE_KEY = "com.example.jamin.teamivchecker.PREFERENCE_FILE_KEY";



    // Creating background thread to import CSV data
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ImportFromCSV csv = new ImportFromCSV(getApplicationContext());
            csv.importFromCSV();
            csv.importCPMFromCSV();


            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            csv.exportToNiaDatabase(mNotificationManager, mNotificationBuilder);
            csv.close();

            // Remove notification
            mNotificationManager.cancel(NOTIFICATION_ID);

        }
    };

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
        numTessBaseApi = new TessBaseAPI();
        String datapath = Environment.getExternalStorageDirectory() + "/TeamIVChecker/";
        String language = "eng";
        // AssetManager assetManager = getAssets();
        File dir = new File(datapath + "/tessdata/");
        if (!dir.exists())
            dir.mkdirs();
        tessBaseApi.init(datapath, language);
        numTessBaseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_OSD_ONLY);
        numTessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,"0123456789");
        numTessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST,"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmopqrstuvwxyz");
        numTessBaseApi.init(datapath, language);
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


        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        boolean isLoaded = sharedPref.getBoolean("loaded", false); // Check if database is already imported

        if (!isLoaded) { // Import from CSV if our database is empty
            // Testing attempt to create new background thread
            new Thread(runnable).start();

            // prepare a notification for user and start service foreground
            mNotificationBuilder = new Notification.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Generating Database")
                    .setSmallIcon(R.drawable.ic_sentiment_satisfied_black_24dp)
                    .setOngoing(true)
                    .setProgress(100, 0, false);

            Notification notification = mNotificationBuilder.build();

            // this will ensure your service won't be killed by Android
            startForeground(NOTIFICATION_ID, notification);

            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putBoolean("loaded", true); // Set flag to true.
            edit.apply();
        }
    }

    @Override
    public void onScreenCaptured(String path) {
        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        Log.d("onScreenCaptured: ", path);
        ScreenshotEditor editor = new ScreenshotEditor(path, display);
        IntegerPoint[][] gridReferencePoints = editor.constructGrid();
        int[][] ivThreshold = new int[ScreenshotEditor.MAXIMUM_COLUMN_COUNT][ScreenshotEditor.MAXIMUM_ROW_COUNT]; // Initialize a default column x row int grid for determining if each CP is above 95% IV. {0: undefined, 1: below threshold, 2: above threshold, 3: 100% possibility}
        OCRData debugData[][] = new OCRData[ScreenshotEditor.MAXIMUM_COLUMN_COUNT][ScreenshotEditor.MAXIMUM_ROW_COUNT]; // Initialize the 2D arrray

        // Fill debugData 2D array with default filler OCRData objects
        for (int i = 0; i < ScreenshotEditor.MAXIMUM_COLUMN_COUNT; i++) {
            for (int j = 0; j < ScreenshotEditor.MAXIMUM_ROW_COUNT; j++) {
                debugData[i][j] = new OCRData();
            }
        }


        final WindowManager.LayoutParams paramsOverlay = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
        );

        DatabaseHelper mDBHelper = new DatabaseHelper(this);
        mDBHelper.connect();
        //        mDBHelper.manualSQL();


        // Getting the info of all pokemons in the grid
        for (int i = 0; i < ScreenshotEditor.MAXIMUM_COLUMN_COUNT; i++) {
            for (int j = 0; j < ScreenshotEditor.MAXIMUM_ROW_COUNT; j++) {
                Bitmap croppedImg = editor.cropImage(gridReferencePoints, i, j);
                Bitmap nameImg = editor.cropName(gridReferencePoints, i, j);
                String cp = "Undefined"; // initialize variables
                String name = "Undefined"; // initialize variables
                String cpString = "";

                if (tessBaseApi != null && numTessBaseApi != null && croppedImg != null && nameImg != null) {
                    numTessBaseApi.setImage(croppedImg);
                    cpString = numTessBaseApi.getUTF8Text();

                    if (cpString.length() > 0) {
                        cp = stripNonDigits(cpString); //cpString.substring(2);
                    } else {
                        Log.d("TESSERACT-OCR", "Invalid CP!");
                        continue;
                    }

                    tessBaseApi.setImage(nameImg);
                    name = tessBaseApi.getUTF8Text();

                    Log.d("TESSERACT-OCR", "String: " + cpString + ", Name: " + name + ", CP: " + cpString);



                    // Check if name registers in database.
                    boolean doesExist = mDBHelper.doesPkmnExist(name);
                    if (!doesExist) {
                        // if name is not found (probably from OCR error, we use levenshtein
                        int bestDist = 100000; // Large distance as default
                        String bestMatch = "";
                        for (int k = 0; k < PokemonList.names.length; k++) {
                            String curName = PokemonList.names[k].toLowerCase();
                            int levDist = Levenshtein.distance(name, curName);
                            if (levDist < bestDist) {
                                bestDist = levDist;
                                bestMatch = curName;
                            }

                            if (levDist == 0) { // This should never happen
                                Log.d(TAG, "Lev found distance 0! SOMETHING IS WRONG!");
                                return;
                            }
                        }
                        Log.d(TAG, "Lev replaced '" + name + "' with '" + bestMatch + "'");
                        name = bestMatch; // overwrite the OCR name with the best matching name
                        //           ivThreshold[i][j] = 0;
                        //           continue;
                    } else {
                        ivThreshold[i][j] = 1;
                    }



                    ArrayList<PokemonIVs> results = mDBHelper.getIVs(name, Integer.parseInt(cp.trim()));

                    int totalIV = -1;
                    if (results.size() > 0) {
                        // Found matching IV. Should always happen unless name is mismatched from database. Will implement name correction later
                        PokemonIVs topIV = results.get(0);
                        totalIV = topIV.getAtk() + topIV.getDef() + topIV.getSta();
                        if (totalIV == 45) { // If it's a candidate for 100%
                            ivThreshold[i][j] = 3;
                        } else if (totalIV >= 43) { // at least threshold (95%)
                            ivThreshold[i][j] = 2;
                        }
                    }

                    Log.d(TAG, "DEBUG: CP: " + cp + " , IV: " + String.valueOf(totalIV));
                    debugData[i][j] = new OCRData(cp, String.valueOf(Math.round((totalIV/45.0) * 1000)/10)); // String converts IV to percentage with 1 decimal point

                }
            }
        }


        mDBHelper.close();

        mOverlayView = new OverlayView(this, gridReferencePoints, ivThreshold, debugData);
        windowManager.addView(mOverlayView, paramsOverlay);
        isOverlayOn = true;


// Test database


        /*
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.connect();
        Pokemon pkmn = dbHelper.selectPokemonByName("Alakazam");
        dbHelper.printPokemonObject(pkmn);
        dbHelper.close();
        */
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


    // from https://stackoverflow.com/questions/4030928/extract-digits-from-a-string-in-java
    public String stripNonDigits(
            final CharSequence input /* inspired by seh's comment */){
        final StringBuilder sb = new StringBuilder(
                input.length() /* also inspired by seh's comment */);
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
        }
        return sb.toString();
    }

}