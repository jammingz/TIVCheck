package com.example.jamin.teamivchecker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;

public class ScreenshotEditor {
    Bitmap mBitmap;
    int height;
    int width;

    public static final int MAXIMUM_ROW_COUNT = 4;      // Number of rows of pokemon to OCR
    public static final int MAXIMUM_COLUMN_COUNT = 3;   // Number of columns of pokemon to OCR

    public ScreenshotEditor(String path, Display display) {
        File imageFile = new File(path);
        mBitmap = null; // Default is null
        if (imageFile.exists()) {
            // Load the image from file
            mBitmap = BitmapFactory.decodeFile(path);
        }

        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }


    public ScreenshotEditor(Bitmap screenshot, Display display) {
        mBitmap = screenshot;

        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

    }


    public boolean isEqual(RGBColor a, RGBColor b) {
        if (Math.abs(a.getBlue() - b.getBlue()) < 5 && Math.abs(a.getGreen() - b.getGreen()) < 5 && Math.abs(a.getRed() - b.getRed()) < 5) { // if the RGB values are closely similar
            return true;
        }
        return false;
    }

    public IntegerPoint[][] constructGrid() {
        int x = 540;
        int y = 960;
        RGBColor target = new RGBColor(109,237,183);
        int length = 0;

        for (int i = y; i < height; i++) {
            RGBColor color = getRGB(x,i);
            if (!isEqual(target, color)) {
                length++;
            } else {
                break;
            }
        }


        Log.d("constructGrid()", "Length from center:" + String.valueOf(length));


        IntegerPoint healthBarPoint = new IntegerPoint(540, 960 + length);
        return getPositionCoordinates(healthBarPoint);
    }



    private IntegerPoint[][] getPositionCoordinates(IntegerPoint pointOfReference) {
        // BottomRight: (359,721)
        //  TopLeft: (371,721)
        // scrollview top border starts at y=270
        IntegerPoint[][] results = new IntegerPoint[MAXIMUM_COLUMN_COUNT][MAXIMUM_ROW_COUNT]; // Initialize as empty array of 9 empty points.

        for (int i = 0; i < MAXIMUM_COLUMN_COUNT; i++) {
            for (int j = 0; j < MAXIMUM_ROW_COUNT; j++) {
                results[i][j] = new IntegerPoint();
            }
        }


        int refX = pointOfReference.getX();
        int refY = pointOfReference.getY();

        // Determine pointOfReference's position in the array
        int arrayXPos = refX / 342; // width of frame(330) + 12 pixel gap. Means it's arrayXPosition from the left of the screen
        int arrayYPos = ((refY - 270 - 358 - 328) / 391) + 1; // arrayYPositions from the top of the scrollView. 270 is the length of margin above the scrollView. 358 is the length of the top of rectangle to the health bar


        // Get the first(top left corner) position's coordinates
        int x = refX - 165 - 342 * arrayXPos; // 165 is the length between current X position and the left border
        int y = refY - 358 - 391 * arrayYPos; // 358 is the length between HP bar and the top border

        for (int i = 0; i < MAXIMUM_COLUMN_COUNT; i++) {
            for (int j = 0; j < MAXIMUM_ROW_COUNT; j++) {
                results[i][j] = new IntegerPoint(x + 342 * i, y + 391 * j);
            }
        }


        String debugString = "[";
        for (int i = 0; i < MAXIMUM_COLUMN_COUNT; i++) {
            for (int j = 0; j < MAXIMUM_ROW_COUNT; j++) {
                IntegerPoint pointOfInterest = results[i][j];
                debugString += "(" + String.valueOf(pointOfInterest.getX()) + "," + String.valueOf(pointOfInterest.getY()) + "), ";
            }
        }

        debugString += "]";

        Log.d("getPositionCoords()", debugString);

        return results;

    }



    public Bitmap cropImage(IntegerPoint[][] positions, int indexX, int indexY) {
        if (mBitmap != null) {
            IntegerPoint origin = positions[indexX][indexY];
            int originX = origin.getX();
            int originY = origin.getY();
            Bitmap croppedBmp = Bitmap.createBitmap(mBitmap, originX+50, originY+63 , 200 , 50);
            return croppedBmp;
        }

        return null;
    }

    public Bitmap cropName(IntegerPoint[][] positions, int indexX, int indexY) {
        if (mBitmap != null) {
            IntegerPoint origin = positions[indexX][indexY];
            int originX = origin.getX();
            int originY = origin.getY();
            Bitmap croppedBmp = Bitmap.createBitmap(mBitmap, originX+44, originY+300 , 245 , 42);
            return croppedBmp;
        }

        return null;
    }


    /*

     *  Unused Methods that may be important
     */

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

    /*
     *  Helper Classes
     */


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


    /*
     * Debugger Methods
     */


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



}
