package com.example.jamin.teamivchecker;

import android.content.ContentResolver;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class ScreenshotObserver extends ContentObserver {
    private final String[] PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA
    };

    private final String MEDIA_EXTERNAL_URI_STRING = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
    private final String FILE_NAME_PREFIX = "screenshot";
    private final String PATH_SCREENSHOT = "screenshots/";

    private ContentResolver mContentResolver;
    private final ScreenshotCheck.Listener mListener;

    public ScreenshotObserver(Handler handler, ContentResolver contentResolver, ScreenshotCheck.Listener listener) {
        super(handler);
        mContentResolver = contentResolver;
        mListener = listener;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.d("ScreenshotObserver","Screenshot detected!");
        if (isSingleImageFile(uri)) {
            handleItem(uri);
        }
    }

    private boolean isSingleImageFile(Uri uri) {
        Log.d("isSingleImageFile", "[" + uri.toString() + " vs " + MEDIA_EXTERNAL_URI_STRING + "]");
        Log.d("isSingleImageFile" , "Boolean: " + String.valueOf(uri.toString().matches(MEDIA_EXTERNAL_URI_STRING + "/[0-9]+")));
        return uri.toString().matches(MEDIA_EXTERNAL_URI_STRING + "/[0-9]+");
    }

    private void handleItem(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(uri, PROJECTION, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                final ScreenshotData screenshotData = generateScreenshotDataFromCursor(cursor);
                if (screenshotData != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onScreenShotTaken(screenshotData);
                        }
                    });
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private ScreenshotData generateScreenshotDataFromCursor(Cursor cursor) {
        final long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        final String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
        final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

        if (isPathScreenshot(path) && isFileScreenshot(fileName)) {
            return new ScreenshotData(id, fileName, path);
        } else {
            return null;
        }
    }

    private boolean isFileScreenshot(String fileName) {
        return fileName.toLowerCase().startsWith(FILE_NAME_PREFIX);
    }

    private boolean isPathScreenshot(String path) {
        return path.toLowerCase().contains(PATH_SCREENSHOT);
    }
}
