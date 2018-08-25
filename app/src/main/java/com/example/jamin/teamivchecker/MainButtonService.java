package com.example.jamin.teamivchecker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class MainButtonService extends Service implements ScreenshotDetectionDelegate.ScreenshotDetectionListener{
    private static final String TAG = "MainButtonService";
    static final String EXTRA_RESULT_CODE="resultCode";
    static final String EXTRA_RESULT_INTENT="resultIntent";
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



    // Variables for ScreenCapture

    private static MediaProjection mMediaProjection;

    private MediaProjectionManager mMediaProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private static String STORE_DIRECTORY;
    private int resultCode;
    private Intent resultData;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private int IMAGES_PRODUCED;
    private boolean screenCaptureLock;
    private int screenshotCode;
    private int[] detailScreenshotCPs = new int[4]; // Struture to hold the 3 CPs of the pokemon in detailed view. Used to find the most occurying CP to eliminate OCR error that may result in a single scan.


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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 1337);
        resultData = intent.getParcelableExtra(EXTRA_RESULT_INTENT);
        return(START_NOT_STICKY);
    }

    @Override public void onCreate() {
        super.onCreate();

        isOverlayOn = false;

        Log.d("MainButtonService", "onCreate() called");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        // Setting up ScreenRecording variables
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // start capture handling thread
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();

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
        screenCaptureLock = false;


        // Setting up navigation button
        mainButton = new ImageView(this);
        mainButton.setImageResource(R.drawable.ic_sentiment_satisfied_black_36dp);
        // mainButton.setBackgroundColor(Color.GRAY);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 200;
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
                    if (!screenCaptureLock) { // Button should only work when lock isn't used. Aka overlay isn't already being generated concurrently
                        if (isOverlayOn && mOverlayView != null) { // Remove overlay
                            windowManager.removeView(mOverlayView);
                            //windowManager.updateViewLayout(mOverlayView, paramsOverlay);
                            isOverlayOn = false;
                            // stopScreenCapture();
                        } else { // Scan screen and create overlay
                            IMAGES_PRODUCED = 0;
                            screenshotCode = 0;
                            startScreenCapture();
                        }
                    }
                    return true;
                } else {
                    // code for move and drag
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (params.x < 600 && params.x > 450 && params.y < 1600 && params.y > 1400) {
                                // exit service
                                finish();
                            }

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

        /*
        DatabaseHelper mDBHelper = new DatabaseHelper(getApplicationContext());
        mDBHelper.connect();
        mDBHelper.manualSQL();
        mDBHelper.close();

        */
    }

    @Override
    public void onScreenCaptured(String path) {
        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        Log.d("onScreenCaptured: ", path);
        ScreenshotEditor editor = new ScreenshotEditor(path, display);

        if (editor.determineScreenCapture() == editor.STATS_DETAIL_SCREEN) { // Check IV in stats page
            Bitmap detailCpImage = editor.cropDetailCP();
            Bitmap detailNameImage = editor.cropDetailName();
            Bitmap detailHpImage = editor.cropDetailHp();
            Bitmap detailDustImage = editor.cropDetailDust();


            String cp = "Undefined"; // initialize variables
            String name = "Undefined"; // initialize variables
            String hp = "Undefined";
            String dust = "Undefined";
            String cpString = "";
            String dustString = "";
            String hpString = "";

            if (tessBaseApi != null && numTessBaseApi != null) {
                numTessBaseApi.setImage(detailCpImage);
                cpString = numTessBaseApi.getUTF8Text();

                numTessBaseApi.setImage(detailHpImage);
                hpString = numTessBaseApi.getUTF8Text();

                numTessBaseApi.setImage(detailDustImage);
                dustString = numTessBaseApi.getUTF8Text();

                if (cpString.length() > 0) {
                    cp = stripNonDigits(cpString); //cpString.substring(2);
                } else {
                    Log.d("TESSERACT-OCR", "Invalid CP!");
                }

                if (hpString.length() > 0) {
                    hp = stripNonDigits(hpString); //cpString.substring(2);
                } else {
                    Log.d("TESSERACT-OCR", "Invalid CP!");
                }

                if (dustString.length() > 0) {
                    dust = stripNonDigits(dustString); //cpString.substring(2);
                } else {
                    Log.d("TESSERACT-OCR", "Invalid CP!");
                }

                tessBaseApi.setImage(detailNameImage);
                name = tessBaseApi.getUTF8Text();

                Log.d("TESSERACT-OCR", "Pokemon Name: " + name);

                int bestDist = 100000; // Large distance as default
                String bestMatch = "";
                for (int k = 0; k < PokemonList.names.length; k++) {
                    String curName = PokemonList.names[k].toLowerCase();
                    Log.d(TAG, "Comparing " + curName + " with " + name);
                    int levDist = Levenshtein.distance(name, curName);
                    if (levDist < bestDist) {
                        bestDist = levDist;
                        bestMatch = curName;
                    }

                    if (levDist == 0) { // Found the match!
                        Log.d(TAG, "Match found!: " + curName + " == " + name);
                        break;
                    }
                }

                name = bestMatch; // overwrite the OCR name with the best matching name

                DatabaseHelper mDBHelper = new DatabaseHelper(getApplicationContext());
                mDBHelper.connect();
                ArrayList<PokemonIVs> ivList = mDBHelper.getIVs(name, Integer.parseInt(cp), Integer.parseInt(hp), CalculateCP.dustToLevel(Integer.parseInt(dust)));
                mDBHelper.close();
                Log.d(TAG, "OCR-DETAILSCREEN: [" + name + "(" + cp + "): [" + hp + "HP / " + dust + " dust]");



                String iv;
                String range;

                if (ivList.size() > 0) {
                    int bestIV;
                    if (ivList.size() == 1) {
                        int atk = ivList.get(0).getAtk();
                        int sta = ivList.get(0).getSta();
                        int def = ivList.get(0).getDef();

                        bestIV = Math.round((atk + def + sta) * 100 / 45);
                        // Log.d(TAG, "IV : " + String.valueOf(bestIV) + "%");

                        iv = String.valueOf(atk) + "-" + String.valueOf(def) + "-" + String.valueOf(sta);
                        range = String.valueOf(bestIV) + "%";

                    } else {
                        int worstIV;
                        // IVs fetched

                        // Calculate max IV

                        int atk = ivList.get(0).getAtk();
                        int sta = ivList.get(0).getSta();
                        int def = ivList.get(0).getDef();

                        bestIV = Math.round((atk + def + sta) * 100 / 45);

                        // Calculate worst IV

                        atk = ivList.get(ivList.size()-1).getAtk();
                        sta = ivList.get(ivList.size()-1).getSta();
                        def = ivList.get(ivList.size()-1).getDef();

                        worstIV = Math.round((atk + def + sta) * 100 / 45);

                        range = String.valueOf(worstIV) + "-" + String.valueOf(bestIV) + "%";
                        iv = null;

                        //  Log.d(TAG, "IV RANGE: " + String.valueOf(worstIV) + "-" + String.valueOf(bestIV) + "%");
                    }





                } else {
                    // Garbage IVs

                    iv = null;
                    range = "GARBAGE";
                }

                final WindowManager.LayoutParams paramsOverlay = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        PixelFormat.TRANSLUCENT
                );

                mOverlayView = new OverlayView(getApplicationContext(), range, iv, name, cp, dust, hp);
                windowManager.addView(mOverlayView, paramsOverlay);
                isOverlayOn = true;
            }
        } else {

            IntegerPoint[][] gridReferencePoints = editor.constructGrid();
            int rowSize = 0;
            int columnSize = 0;
            if (gridReferencePoints.length == 0 || gridReferencePoints[0].length == 0) {
                return;
            } else {
                columnSize = gridReferencePoints.length;
                rowSize = gridReferencePoints[0].length;
            }


            int[][] ivThreshold = new int[columnSize][rowSize]; // Initialize a default column x row int grid for determining if each CP is above 95% IV. {0: undefined, 1: below threshold, 2: above threshold, 3: 100% possibility}
            OCRData debugData[][] = new OCRData[columnSize][rowSize]; // Initialize the 2D arrray

            // Fill debugData 2D array with default filler OCRData objects
            for (int i = 0; i < columnSize; i++) {
                for (int j = 0; j < rowSize; j++) {
                    debugData[i][j] = new OCRData();
                }
            }


            final WindowManager.LayoutParams paramsOverlay = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    PixelFormat.TRANSLUCENT
            );

            DatabaseHelper mDBHelper = new DatabaseHelper(getApplicationContext());
            mDBHelper.connect();
            //        mDBHelper.manualSQL();


            // Getting the info of all pokemons in the grid
            for (int i = 0; i < columnSize; i++) {
                for (int j = 0; j < rowSize; j++) {
                    Bitmap croppedImg = editor.cropImage(gridReferencePoints, i, j);
                    Bitmap nameImg = editor.cropName(gridReferencePoints, i, j);
                    String cp = "Undefined"; // initialize variables
                    String name = "Undefined"; // initialize variables
                    String cpString = "";

                    if (tessBaseApi != null && numTessBaseApi != null && croppedImg != null && nameImg != null) {
                        long startTime = System.nanoTime();
                        numTessBaseApi.setImage(croppedImg);
                        cpString = numTessBaseApi.getUTF8Text();
                        long endTime = System.nanoTime();
                        long duration = (endTime - startTime);

                        Log.d("TIMIT", "OCR-CP: " + String.valueOf(duration / 1000000));

                        if (cpString.length() > 0) {
                            cp = stripNonDigits(cpString); //cpString.substring(2);
                        } else {
                            Log.d("TESSERACT-OCR", "Invalid CP!");
                            continue;
                        }

                        startTime = System.nanoTime();

                        tessBaseApi.setImage(nameImg);
                        name = tessBaseApi.getUTF8Text();

                        endTime = System.nanoTime();
                        duration = (endTime - startTime);

                        Log.d("TIMIT", "OCR-Name: " + String.valueOf(duration / 1000000));


                        // Log.d("TESSERACT-OCR", "String: " + cpString + ", Name: " + name + ", CP: " + cpString);


                        startTime = System.nanoTime();

                        // Check if name registers in database.
                        //     boolean doesExist = mDBHelper.doesPkmnExist(name);

                        boolean doesExist = false;
                        endTime = System.nanoTime();
                        duration = (endTime - startTime);

                        Log.d("TIMIT", "Check Existance: " + String.valueOf(duration / 1000000));

                        if (!doesExist) {
                            // if name is not found (probably from OCR error, we use levenshtein


                            startTime = System.nanoTime();
                            int bestDist = 100000; // Large distance as default
                            String bestMatch = "";
                            for (int k = 0; k < PokemonList.names.length; k++) {
                                String curName = PokemonList.names[k].toLowerCase();
                                int levDist = Levenshtein.distance(name, curName);
                                if (levDist < bestDist) {
                                    bestDist = levDist;
                                    bestMatch = curName;
                                }

                                if (levDist == 0) { // Found the match!
                                    ivThreshold[i][j] = 1;
                                    break;
                                }
                            }
                            //                        Log.d(TAG, "Lev replaced '" + name + "' with '" + bestMatch + "'");
                            name = bestMatch; // overwrite the OCR name with the best matching name
                            //           ivThreshold[i][j] = 0;
                            //           continue;


                            endTime = System.nanoTime();
                            duration = (endTime - startTime);

                            Log.d("TIMIT", "Levenshtein Time: " + String.valueOf(duration / 1000000));
                        } else {
                            ivThreshold[i][j] = 1;
                        }


                        startTime = System.nanoTime();
                        ArrayList<PokemonIVs> results = mDBHelper.getIVs(name, Integer.parseInt(cp.trim()));

                        endTime = System.nanoTime();
                        duration = (endTime - startTime);

                        Log.d("TIMIT", "Database Fetch IVs: " + String.valueOf(duration / 1000000));
                        int totalIV = -1;
                        if (results.size() > 0) {
                            // Found matching IV. Should always happen unless name is mismatched from database. Will implement name correction later
                            PokemonIVs topIV = results.get(0);
                            totalIV = topIV.getAtk() + topIV.getDef() + topIV.getSta();
                            if (totalIV == 45) { // If it's a candidate for 100%
                                ivThreshold[i][j] = 3;
                            } else if (totalIV >= 42) { // at least threshold (93%)
                                ivThreshold[i][j] = 2;
                            }
                        }

                        //                           Log.d(TAG, "DEBUG: CP: " + cp + " , IV: " + String.valueOf(totalIV));
                        debugData[i][j] = new OCRData(cp, String.valueOf(Math.round((totalIV / 45.0) * 1000) / 10)); // String converts IV to percentage with 1 decimal point

                    }
                }
            }

            mDBHelper.close();

            mOverlayView = new OverlayView(getApplicationContext(), gridReferencePoints, ivThreshold, debugData);
            windowManager.addView(mOverlayView, paramsOverlay);
            isOverlayOn = true;
        }

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
            // Log.d(TAG, "Char at " + String.valueOf(c));
            if(c > 47 && c < 58){

                sb.append(c);
            } else if (c == 79 || c == 111) {
                sb.append('0'); // Append 0 in case OCR misinterprets it as 'o' or 'O'
            }
        }
        return sb.toString();
    }


    public Bitmap takeScreenshot(View v) {
        View rootView = v.getRootView();
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache(true);
        Bitmap bmp = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);
        return bmp;

    }

    public void finish() {
        this.stopSelf();
        Log.d(TAG, "MainButtonService stopSelf()");
    }



    // Screenshot Capture Methods
    private void startScreenCapture() {
        screenCaptureLock = true; // Acquire lock
        Log.e("ScreenCapture", "Starting projection.");
        mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, resultData);

        if (mMediaProjection != null) {
            File externalFilesDir = getExternalFilesDir(null);
            if (externalFilesDir != null) {
                STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/screenshots/";
                File storeDirectory = new File(STORE_DIRECTORY);
                if (!storeDirectory.exists()) {
                    boolean success = storeDirectory.mkdirs();
                    if (!success) {
                        Log.e(TAG, "failed to create file storage directory.");
                        return;
                    }
                }
            } else {
                Log.e(TAG, "failed to create file storage directory, getExternalFilesDir is null.");
                return;
            }

            // display metrics
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            mDensity = metrics.densityDpi;
            mDisplay = windowManager.getDefaultDisplay();

            // create virtual display depending on device width / height
            createVirtualDisplay();


            // register media projection stop callback
            mMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
        }
    }

    private void stopScreenCapture() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaProjection != null) {
                    mMediaProjection.stop();
                }
            }
        });
        screenCaptureLock = false; // release lock
    }

    private void showStatusIcon() {
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCapture", "stopping projection.");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                    mMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }

    private void createVirtualDisplay() {
        // get width and height
        Point size = new Point();
        mDisplay.getRealSize(size);
        mWidth = size.x;
        mHeight = size.y;

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
    }



    // When listener is recording and receives screenshot
    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "SS Code: " + String.valueOf(screenshotCode) + ", IMAGES_PRODUCED: " + String.valueOf(IMAGES_PRODUCED));
            if ((IMAGES_PRODUCED >= 1 && screenshotCode == 0) || (IMAGES_PRODUCED >=3 && screenshotCode == 1)) { // Base case. When to exit loop. screenshotcode = 0 when it's a new screenshot. screenshotcode = 1 when it's in the progress of taking the multiple screenshots of detailed page
                Log.d(TAG, "stopScreenCapture()");
                stopScreenCapture(); // Stop scanning. Base case reached
                return;
            }
            IMAGES_PRODUCED++; // Otherwise we increment counter by 1

            Image image = null;
            Bitmap bitmap = null;

            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    // Log.e(TAG, "onImageAvailable()");

                    WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    Display display = window.getDefaultDisplay();

                    ScreenshotEditor editor = new ScreenshotEditor(bitmap, display);

                    if (editor.determineScreenCapture() == editor.STATS_DETAIL_SCREEN) { // Check IV in stats page
                        // Write bmp into storage

                        Log.d(TAG, "Writing to " + DATA_PATH + "Screenshot" + String.valueOf(IMAGES_PRODUCED)+".png");


                        screenshotCode = 1;
                        Bitmap detailCpImage = editor.cropDetailCP();
                        Bitmap detailNameImage = editor.cropDetailName();
                        Bitmap detailHpImage = editor.cropDetailHp();
                        Bitmap detailDustImage = editor.cropDetailDust();

                        File dir = new File(DATA_PATH);
                        if (!dir.exists()) {
                            dir.mkdir();
                        }

                        File newFile = new File(DATA_PATH + "Screenshot" + String.valueOf(IMAGES_PRODUCED)+".png");
                        /*
                        if (newFile.exists()) {
                            Log.d(TAG, "Deleting old file!");
                            newFile.delete();
                        }
                        */

                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(newFile, false);
                            detailCpImage.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                            // PNG is a lossless format, the compression factor (100) is ignored
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (out != null) {
                                    out.flush();
                                    out.close();
                                } else {
                                    Log.d(TAG, "bmp is null");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                        String cp = "Undefined"; // initialize variables
                        String name = "Undefined"; // initialize variables
                        String hp = "Undefined";
                        String dust = "Undefined";
                        String cpString = "";
                        String dustString = "";
                        String hpString = "";

                        if (tessBaseApi != null && numTessBaseApi != null) {
                            numTessBaseApi.setImage(detailCpImage);
                            cpString = numTessBaseApi.getUTF8Text();
                            Log.d(TAG, "cpString: " + cpString);

                            if (cpString.length() > 0) {
                                cp = stripNonDigits(cpString); //cpString.substring(2);
                                Log.d(TAG, "strippedCP: " + cp);
                                detailScreenshotCPs[IMAGES_PRODUCED] = Integer.valueOf(cp);
                            } else {
                                Log.d("TESSERACT-OCR", "Invalid CP!");
                                detailScreenshotCPs[IMAGES_PRODUCED] = 0;
                            }

                            if (IMAGES_PRODUCED < 3) {
                                return;
                            }

                            // Now that we have multiple samples of CP, we choose the most frequent value

                            // First we construct hashmap for O(n) running time
                            Map<Integer, Integer> hashCP = new HashMap<Integer, Integer>();


                            for (int i = 1; i < detailScreenshotCPs.length; i++) {
                                int curCP = detailScreenshotCPs[i];
                                Log.d(TAG, "Counting CPs: " + String.valueOf(curCP));
                                if (hashCP.containsKey(curCP)) { // increment frequency if key already exist
                                    int freqency = hashCP.get(curCP);
                                    freqency++;
                                    hashCP.put(curCP, freqency);
                                } else { // create new key with default value of 1
                                    hashCP.put(curCP, 1);
                                }
                            }

                            int maxCount = 0;
                            int mostFreqCP = 0;
                            // Iterate through hashmap to find most frequent value
                            for (Map.Entry<Integer, Integer> entry : hashCP.entrySet()) {
                                if (entry.getValue() > maxCount) {
                                    mostFreqCP = entry.getKey();
                                    maxCount = entry.getValue();
                                }
                            }

                            cp = String.valueOf(mostFreqCP);

                            numTessBaseApi.setImage(detailHpImage);
                            hpString = numTessBaseApi.getUTF8Text();

                            numTessBaseApi.setImage(detailDustImage);
                            dustString = numTessBaseApi.getUTF8Text();


                            if (hpString.length() > 0) {
                                hp = stripNonDigits(hpString); //cpString.substring(2);
                            } else {
                                Log.d("TESSERACT-OCR", "Invalid CP!");
                            }

                            if (dustString.length() > 0) {
                                dust = stripNonDigits(dustString); //cpString.substring(2);
                            } else {
                                Log.d("TESSERACT-OCR", "Invalid CP!");
                            }

                            tessBaseApi.setImage(detailNameImage);
                            name = tessBaseApi.getUTF8Text();
                            String debugTest = name;

                            int bestDist = 100000; // Large distance as default
                            String bestMatch = "";
                            for (int k = 0; k < PokemonList.names.length; k++) {
                                String curName = PokemonList.names[k].toLowerCase();
                                Log.d(TAG, "Comparing " + curName + " with " + name);
                                int levDist = Levenshtein.distance(name, curName);
                                if (levDist < bestDist) {
                                    bestDist = levDist;
                                    bestMatch = curName;
                                }

                                if (levDist == 0) { // Found the match!
                                    break;
                                }
                            }

                            name = bestMatch; // overwrite the OCR name with the best matching name


                            Log.d(TAG, "OCR-DETAILSCREEN: [" + name + "(" + cp + "): [" + hp + "HP / " + dust + " dust]");
                            DatabaseHelper mDBHelper = new DatabaseHelper(getApplicationContext());
                            mDBHelper.connect();
                            ArrayList<PokemonIVs> ivList = mDBHelper.getIVsFromDetail(getApplicationContext(), name, Integer.parseInt(cp), Integer.parseInt(hp), CalculateCP.dustToLevel(Integer.parseInt(dust)));
                            mDBHelper.close();


                            String iv;
                            String range;

                            if (ivList.size() > 0) {
                                Collections.sort(ivList);
                                int bestIV;
                                if (ivList.size() == 1) {
                                    int atk = ivList.get(0).getAtk();
                                    int sta = ivList.get(0).getSta();
                                    int def = ivList.get(0).getDef();

                                    bestIV = Math.round((atk + def + sta) * 100 / 45);
                                    // Log.d(TAG, "IV : " + String.valueOf(bestIV) + "%");

                                    iv = String.valueOf(atk) + "-" + String.valueOf(def) + "-" + String.valueOf(sta);
                                    range = String.valueOf(bestIV) + "%";

                                } else {
                                    int worstIV;
                                    // IVs fetched

                                    // Calculate worst IV

                                    int atk = ivList.get(0).getAtk();
                                    int sta = ivList.get(0).getSta();
                                    int def = ivList.get(0).getDef();

                                    worstIV = Math.round((atk + def + sta) * 100 / 45);

                                    // Calculate best IV

                                    atk = ivList.get(ivList.size()-1).getAtk();
                                    sta = ivList.get(ivList.size()-1).getSta();
                                    def = ivList.get(ivList.size()-1).getDef();

                                    bestIV = Math.round((atk + def + sta) * 100 / 45);

                                    range = String.valueOf(worstIV) + "-" + String.valueOf(bestIV) + "%";
                                    iv = null;

                                  //  Log.d(TAG, "IV RANGE: " + String.valueOf(worstIV) + "-" + String.valueOf(bestIV) + "%");
                                }





                            } else {
                                // Garbage IVs

                                iv = null;
                                range = "GARBAGE";
                            }

                            final WindowManager.LayoutParams paramsOverlay = new WindowManager.LayoutParams(
                                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                    PixelFormat.TRANSLUCENT
                            );

                            mOverlayView = new OverlayView(getApplicationContext(), range, iv, debugTest, cp, dust, hp);
                            windowManager.addView(mOverlayView, paramsOverlay);
                            isOverlayOn = true;
                        }
                    } else { // It's a storage screenshot

                        IntegerPoint[][] gridReferencePoints = editor.constructGrid();
                        int rowSize = 0;
                        int columnSize = 0;
                        if (gridReferencePoints.length == 0 || gridReferencePoints[0].length == 0) {
                            return;
                        } else {
                            columnSize = gridReferencePoints.length;
                            rowSize = gridReferencePoints[0].length;
                        }


                        int[][] ivThreshold = new int[columnSize][rowSize]; // Initialize a default column x row int grid for determining if each CP is above 95% IV. {0: undefined, 1: below threshold, 2: above threshold, 3: 100% possibility}
                        OCRData debugData[][] = new OCRData[columnSize][rowSize]; // Initialize the 2D arrray

                        // Fill debugData 2D array with default filler OCRData objects
                        for (int i = 0; i < columnSize; i++) {
                            for (int j = 0; j < rowSize; j++) {
                                debugData[i][j] = new OCRData();
                            }
                        }


                        final WindowManager.LayoutParams paramsOverlay = new WindowManager.LayoutParams(
                                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                PixelFormat.TRANSLUCENT
                        );

                        DatabaseHelper mDBHelper = new DatabaseHelper(getApplicationContext());
                        mDBHelper.connect();
                        //        mDBHelper.manualSQL();


                        // Getting the info of all pokemons in the grid
                        for (int i = 0; i < columnSize; i++) {
                            for (int j = 0; j < rowSize; j++) {
                                Bitmap croppedImg = editor.cropImage(gridReferencePoints, i, j);
                                Bitmap nameImg = editor.cropName(gridReferencePoints, i, j);
                                String cp = "Undefined"; // initialize variables
                                String name = "Undefined"; // initialize variables
                                String cpString = "";

                                if (tessBaseApi != null && numTessBaseApi != null && croppedImg != null && nameImg != null) {
                                    long startTime = System.nanoTime();
                                    numTessBaseApi.setImage(croppedImg);
                                    cpString = numTessBaseApi.getUTF8Text();
                                    long endTime = System.nanoTime();
                                    long duration = (endTime - startTime);

                                    Log.d("TIMIT", "OCR-CP: " + String.valueOf(duration / 1000000));

                                    if (cpString.length() > 0) {
                                        cp = stripNonDigits(cpString); //cpString.substring(2);
                                    } else {
                                        Log.d("TESSERACT-OCR", "Invalid CP!");
                                        continue;
                                    }

                                    startTime = System.nanoTime();
                                    tessBaseApi.setImage(nameImg);
                                    name = tessBaseApi.getUTF8Text();


                                    endTime = System.nanoTime();
                                    duration = (endTime - startTime);

                                    Log.d("TIMIT", "OCR-Name: " + String.valueOf(duration / 1000000));


                                    // Log.d("TESSERACT-OCR", "String: " + cpString + ", Name: " + name + ", CP: " + cpString);

                                    startTime = System.nanoTime();

                                    // Check if name registers in database.
                                    //     boolean doesExist = mDBHelper.doesPkmnExist(name);

                                    boolean doesExist = false;
                                    endTime = System.nanoTime();
                                    duration = (endTime - startTime);

                                    Log.d("TIMIT", "Check Existance: " + String.valueOf(duration / 1000000));

                                    if (!doesExist) {
                                        // if name is not found (probably from OCR error, we use levenshtein


                                        startTime = System.nanoTime();
                                        int bestDist = 100000; // Large distance as default
                                        String bestMatch = "";
                                        for (int k = 0; k < PokemonList.names.length; k++) {
                                            String curName = PokemonList.names[k].toLowerCase();
                                            int levDist = Levenshtein.distance(name, curName);
                                            if (levDist < bestDist) {
                                                bestDist = levDist;
                                                bestMatch = curName;
                                            }

                                            if (levDist == 0) { // Found the match!
                                                ivThreshold[i][j] = 1;
                                                break;
                                            }
                                        }
                                        //                        Log.d(TAG, "Lev replaced '" + name + "' with '" + bestMatch + "'");
                                        name = bestMatch; // overwrite the OCR name with the best matching name
                                        //           ivThreshold[i][j] = 0;
                                        //           continue;


                                        endTime = System.nanoTime();
                                        duration = (endTime - startTime);

                                        Log.d("TIMIT", "Levenshtein Time: " + String.valueOf(duration / 1000000));

                                    } else {
                                        ivThreshold[i][j] = 1;
                                    }

                                    startTime = System.nanoTime();
                                    ArrayList<PokemonIVs> results = mDBHelper.getIVs(name, Integer.parseInt(cp.trim()));

                                    endTime = System.nanoTime();
                                    duration = (endTime - startTime);

                                    Log.d("TIMIT", "Database Fetch IVs: " + String.valueOf(duration / 1000000));
                                    int totalIV = -1;
                                    if (results.size() > 0) {
                                        // Found matching IV. Should always happen unless name is mismatched from database. Will implement name correction later
                                        PokemonIVs topIV = results.get(0);
                                        totalIV = topIV.getAtk() + topIV.getDef() + topIV.getSta();
                                        if (totalIV == 45) { // If it's a candidate for 100%
                                            ivThreshold[i][j] = 3;
                                        } else if (totalIV >= 42) { // at least threshold (93%)
                                            ivThreshold[i][j] = 2;
                                        }
                                    }

                                    //                           Log.d(TAG, "DEBUG: CP: " + cp + " , IV: " + String.valueOf(totalIV));
                                    debugData[i][j] = new OCRData(cp, String.valueOf(Math.round((totalIV / 45.0) * 1000) / 10)); // String converts IV to percentage with 1 decimal point

                                }
                            }
                        }

                        mDBHelper.close();

                        mOverlayView = new OverlayView(getApplicationContext(), gridReferencePoints, ivThreshold, debugData);
                        windowManager.addView(mOverlayView, paramsOverlay);
                        isOverlayOn = true;
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }

            }
        }
    }

}