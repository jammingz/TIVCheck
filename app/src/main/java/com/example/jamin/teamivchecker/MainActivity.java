package com.example.jamin.teamivchecker;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    public final static int REQUEST_CODE = 5462;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        checkDrawOverlayPermission();
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


}
