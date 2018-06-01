package com.example.jamin.teamivchecker;

public class OCRData {
    private String CP;
    private String IV;

    public OCRData() {
        CP = "-1";
        IV = "-1";
    }

    public OCRData(String CP) {
        this.CP = CP;
        IV = "-1";
    }

    public OCRData(String CP, String IV){
        this.CP = CP;
        this.IV = IV;
    }

    public String getCP() {
        return CP;
    }

    public String getIV() {
        return IV;
    }
}
