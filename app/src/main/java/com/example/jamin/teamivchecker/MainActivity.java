package com.example.jamin.teamivchecker;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends ScreenshotDetectionActivity {
    public final static int REQUEST_CODE = 5462;
    private ScreenshotCheck mScreenshotCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        checkDrawOverlayPermission();

        // Screenshot detector credits from https://github.com/abangfadli/shotwatch
        /*
        mScreenshotCheck = new ScreenshotCheck(getContentResolver(), new ScreenshotCheck.Listener() {
            @Override
            public void onScreenShotTaken(ScreenshotData screenshotData) {
                Toast.makeText(getApplicationContext(), screenshotData.getFileName(), Toast.LENGTH_SHORT).show();
            }
        });
        */
    }


    // Overlay code from https://stackoverflow.com/questions/7569937/unable-to-add-window-android-view-viewrootw44da9bc0-permission-denied-for-t
    public void checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            Log.d("MainActivity","Requesting permission to draw over apps");
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */


            startActivityForResult(intent, REQUEST_CODE);
        } else {
            startService(new Intent(getApplicationContext(), MainButtonService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == REQUEST_CODE) {
            Log.d("MainActivity","Request code = REQUEST_CODE");
       // if so check once again if we have permission
            if (Settings.canDrawOverlays(this)) {
                // continue here - permission was granted
                Log.d("MainActivity","App can draw overlays!");
                startService(new Intent(getApplicationContext(), MainButtonService.class));
            }
        } else {

            Log.d("MainActivity","Request code =/= REQUEST_CODE");
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        /*
        mScreenshotCheck.register();
        Toast.makeText(getApplicationContext(), "mScreenshotCheck registered", Toast.LENGTH_SHORT).show();
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
        mScreenshotCheck.unregister();
        Toast.makeText(getApplicationContext(), "mScreenshotCheck unregistered", Toast.LENGTH_SHORT).show();
        */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Stopping service", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainButtonService.class);
        stopService(intent);
    }

    @Override
    public void onScreenCaptured(String path) {

        /*
        Log.d("onScreenCaptured: ", path);
        Toast.makeText(this, path, Toast.LENGTH_SHORT).show();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Debug path
        String path2 = "/storage/emulated/0/Pictures/Screenshots/Screenshot_20180504-145015.png";
        ScreenshotEditor editor = new ScreenshotEditor(path2, displayMetrics);
        editor.testFindPixel();
        editor.testFindPixelDiff();
        editor.testFindHPPixel();
        //editor.extractColor();

*/
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        Toast.makeText(this, "Please grant read external storage permission for screenshot detection", Toast.LENGTH_SHORT).show();
    }
}
