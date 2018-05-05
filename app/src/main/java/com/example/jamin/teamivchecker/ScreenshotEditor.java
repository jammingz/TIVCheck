package com.example.jamin.teamivchecker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

public class ScreenshotEditor {
    Bitmap mBitmap;
    int height;
    int width;

    public ScreenshotEditor(String path, DisplayMetrics metrics) {
        File imageFile = new File(path);
        mBitmap = null; // Default is null
        if (imageFile.exists()) {
            // Load the image from file
            mBitmap = BitmapFactory.decodeFile(path);
        }

        height = metrics.heightPixels;
        width = metrics.widthPixels;
    }

    public RGBColor getRGB(int x, int y) {
        if (mBitmap != null) {
            int pixel = mBitmap.getPixel(x, y);
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);

            return new RGBColor(red, green, blue);
        }

        return new RGBColor();
    }

    public RGBColor extractColor() {
        int center_x = width/2;
        int center_y = height/2;

        RGBColor rgb = getRGB(center_x, center_y);
        int rvalue = rgb.getRed();
        int gvalue = rgb.getGreen();
        int bvalue = rgb.getBlue();

        Log.d("extractColor ", "Pixel: [" + String.valueOf(center_x) + "," + String.valueOf(center_y) + "]  Color: [" + String.valueOf(rvalue) + "," + String.valueOf(gvalue) + "," + String.valueOf(bvalue) + "]");
        return rgb;
    }

    public boolean isEqual(RGBColor a, RGBColor b) {
        if (a.getBlue() == b.getBlue() && a.getGreen() == b.getGreen() && a.getRed() == b.getRed()) {
            return true;
        }
        return false;
    }

    public void testFindPixel() {
        int x = 400;
        int y = 750;
        RGBColor target = new RGBColor(192,235,174);


        int lengthUp = 0;
        int lengthLeft = 0;
        int lengthRight = 0;
        int lengthDown = 0;

        // Down
        for (int i = y; i < height; i++) {
            RGBColor color = getRGB(x,i);
            if (isEqual(target, color)) {
                lengthDown++;
                // Log.d("testFindPixel() ", "Found Identical Pixel at (" + String.valueOf(x) + "," + String.valueOf(i) + ")");
            } else {
                break;
            }
        }

        // Up
        for (int i = y; i >= 0; i--) {
            RGBColor color = getRGB(x,i);
            if (isEqual(target, color)) {
                lengthUp++;
                // Log.d("testFindPixel() ", "Found Identical Pixel at (" + String.valueOf(x) + "," + String.valueOf(i) + ")");
            } else {
                break;
            }
        }

        // Left
        for (int i = x; i >= 0; i--) {
            RGBColor color = getRGB(i,y);
            if (isEqual(target, color)) {
                lengthLeft++;
                // Log.d("testFindPixel() ", "Found Identical Pixel at (" + String.valueOf(x) + "," + String.valueOf(i) + ")");
            } else {
                break;
            }
        }


        // Right
        for (int i = x; i < width; i++) {
            RGBColor color = getRGB(i,y);
            if (isEqual(target, color)) {
                lengthRight++;
                // Log.d("testFindPixel() ", "Found Identical Pixel at (" + String.valueOf(x) + "," + String.valueOf(i) + ")");
            } else {
                break;
            }
        }


        Log.d("Determining Dimensions", "Width: " + String.valueOf(lengthLeft + 1 + lengthRight) + ", Height: " + String.valueOf(lengthUp + 1 + lengthDown) + ", (" + String.valueOf(lengthLeft) + ", " + String.valueOf(lengthUp) + ", " + String.valueOf(lengthRight) + ", " + String.valueOf(lengthDown) + " )");
    }


    public void testFindPixelDiff() { // Finds gap width between selection
        int x = 400;
        int y = 750;
        RGBColor target = new RGBColor(192,235,174);


        int lengthUp = 0;
        int lengthLeft = 0;
        int lengthRight = 0;
        int lengthDown = 0;


        // Up
        for (int i = y; i >= 0; i--) {
            RGBColor color = getRGB(x,i);
            if (isEqual(target, color)) {
                lengthUp++;
                // Log.d("testFindPixel() ", "Found Identical Pixel at (" + String.valueOf(x) + "," + String.valueOf(i) + ")");
            } else {
                break;
            }
        }

        // Left
        for (int i = x; i >= 0; i--) {
            RGBColor color = getRGB(i,y);
            if (isEqual(target, color)) {
                lengthLeft++;
                // Log.d("testFindPixel() ", "Found Identical Pixel at (" + String.valueOf(x) + "," + String.valueOf(i) + ")");
            } else {
                break;
            }
        }




        //Log.d("Determining Dimensions", "Width: " + String.valueOf(lengthLeft + 1 + lengthRight) + ", Height: " + String.valueOf(lengthUp + 1 + lengthDown) + ", (" + String.valueOf(lengthLeft) + ", " + String.valueOf(lengthUp) + ", " + String.valueOf(lengthRight) + ", " + String.valueOf(lengthDown) + " )");
        Log.d("Determining Dimensions", "TopLeft: (" + String.valueOf(x - lengthLeft) + "," + String.valueOf(y - lengthUp) + ")");


        x = 325;
        y = 705;

        // Right
        for (int i = x; i < width; i++) {
            RGBColor color = getRGB(i,y);
            if (isEqual(target, color)) {
                lengthRight++;
                // Log.d("testFindPixel() ", "Found Identical Pixel at (" + String.valueOf(x) + "," + String.valueOf(i) + ")");
            } else {
                break;
            }
        }

        // Down
        for (int i = y; i < height; i++) {
            RGBColor color = getRGB(x,i);
            if (isEqual(target, color)) {
                lengthDown++;
                // Log.d("testFindPixel() ", "Found Identical Pixel at (" + String.valueOf(x) + "," + String.valueOf(i) + ")");
            } else {
                break;
            }
        }

        Log.d("Determining Dimensions", "BottomRight: (" + String.valueOf(x + lengthRight) + "," + String.valueOf(y + lengthDown) + ")");

    }

    private class RGBColor {
        int red;
        int green;
        int blue;

        // Default constructor.
        public RGBColor() {
            red = -1;
            green = -1;
            blue = -1;
        }

        public RGBColor(int r, int g, int b) {
            red = r;
            green = g;
            blue = b;
        }

        public int getBlue() {
            return blue;
        }

        public int getGreen() {
            return green;
        }

        public int getRed() {
            return red;
        }
    }





}
