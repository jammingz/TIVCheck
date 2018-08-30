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

    public static final int STORAGE_SCREEN = 0;
    public static final int STORAGE_SCREEN_WITH_SEARCH = 1;
    public static final int STATS_DETAIL_SCREEN = 2;

    public static final int MAXIMUM_ROW_COUNT = 4;      // Number of rows of pokemon to OCR
    public static final int MINIMUM_ROW_COUNT = 3;
    public static final int MAXIMUM_COLUMN_COUNT = 3;   // Number of columns of pokemon to OCR

    private int screenType;

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

        screenType = determineScreenCapture();
    }


    public ScreenshotEditor(Bitmap screenshot, Display display) {
        mBitmap = screenshot;

        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        screenType = determineScreenCapture();
    }


    public boolean isEqual(RGBColor a, RGBColor b) {
        if (Math.abs(a.getBlue() - b.getBlue()) < 5 && Math.abs(a.getGreen() - b.getGreen()) < 5 && Math.abs(a.getRed() - b.getRed()) < 5) { // if the RGB values are closely similar
            return true;
        }
        return false;
    }


    public int determineScreenCapture() {
        int detailScreenX = 540;
        int detailScreenY = 1196;
        RGBColor detailScreenTarget = new RGBColor(225,225,225);
        RGBColor detailScreenTarget2 = new RGBColor(245,245,245); // There are two colors to check for

        if (isEqual(getRGB(detailScreenX, detailScreenY), detailScreenTarget) || isEqual(getRGB(detailScreenX, detailScreenY), detailScreenTarget2)) {
            return STATS_DETAIL_SCREEN;
        }

        int screenWithSearchX = 722;
        int screenWithSearchY = 388;
        RGBColor screenWithSearchTarget = new RGBColor(70,105,108);

        if (isEqual(getRGB(screenWithSearchX, screenWithSearchY), screenWithSearchTarget)) {
            return STORAGE_SCREEN_WITH_SEARCH;
        }

        return STORAGE_SCREEN;
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

        int columnSize = MAXIMUM_COLUMN_COUNT;
        int rowSize = MAXIMUM_ROW_COUNT;

        if (screenType == STORAGE_SCREEN_WITH_SEARCH) {
            rowSize = MINIMUM_ROW_COUNT;
        }

        IntegerPoint[][] results = new IntegerPoint[columnSize][rowSize]; // Initialize as empty array of 9 empty points.

        for (int i = 0; i < columnSize; i++) {
            for (int j = 0; j < rowSize; j++) {
                results[i][j] = new IntegerPoint();
            }
        }


        int refX = pointOfReference.getX();
        int refY = pointOfReference.getY();

        // Determine pointOfReference's position in the array
        int arrayXPos = refX / 342; // width of frame(330) + 12 pixel gap. Means it's arrayXPosition from the left of the screen
        int arrayYPos = ((refY - 270 - 358 - 328) / 391) + 1; // arrayYPositions from the top of the scrollView. 270 is the length of margin above the scrollView. 358 is the length of the top of rectangle to the health bar

        if (screenType == STORAGE_SCREEN_WITH_SEARCH) {
            arrayYPos = ((refY - 410 - 358 - 328) / 391) + 1; // the margin above scrollview becomes 410 with the search bar offset
        }

        // Get the first(top left corner) position's coordinates
        int x = refX - 165 - 342 * arrayXPos; // 165 is the length between current X position and the left border
        int y = refY - 358 - 391 * arrayYPos; // 358 is the length between HP bar and the top border

        for (int i = 0; i < columnSize; i++) {
            for (int j = 0; j < rowSize; j++) {
                results[i][j] = new IntegerPoint(x + 342 * i, y + 391 * j);
            }
        }


        String debugString = "[";
        for (int i = 0; i < columnSize; i++) {
            for (int j = 0; j < rowSize; j++) {
                IntegerPoint pointOfInterest = results[i][j];
                debugString += "(" + String.valueOf(pointOfInterest.getX()) + "," + String.valueOf(pointOfInterest.getY()) + "), ";
            }
        }

        debugString += "]";

        Log.d("getPositionCoords()", debugString);

        return results;

    }


    // Crops CP
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

    // Crops Name
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

    public Bitmap cropDetailCP() {
        if (mBitmap != null) {
            Bitmap croppedBmp = Bitmap.createBitmap(mBitmap, 370, 115 , 300 , 80);
            return croppedBmp;
        }

        return null;
    }

    public Bitmap cropDetailName() {
        if (mBitmap != null) {
            Bitmap croppedBmp = Bitmap.createBitmap(mBitmap, 305, 819 , 440 , 56);
            return croppedBmp;
        }

        return null;
    }

    public Bitmap cropDetailHp() {
        if (mBitmap != null) {
            Bitmap croppedBmp = Bitmap.createBitmap(mBitmap, 455, 940 , 55 , 25);
            return croppedBmp;
        }

        return null;
    }

    public Bitmap cropDetailDust() {
        if (mBitmap != null) {
            Bitmap croppedBmp = Bitmap.createBitmap(mBitmap, 590, 1425 , 110 , 40);
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
