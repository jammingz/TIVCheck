package com.example.jamin.teamivchecker;

import android.provider.MediaStore;

public class IntegerPoint {
    private int x;
    private int y;

    public IntegerPoint() {
        x = 0;
        y = 0;
    }


    public IntegerPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
