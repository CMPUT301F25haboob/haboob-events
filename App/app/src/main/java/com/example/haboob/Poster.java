package com.example.haboob;

import android.graphics.Picture;

// Not sure exactly what Picture does, maybe we can use this though?
public class    Poster {
    private Picture imgSource;
    private String data;

//    public Poster(Picture imgSource) {
//        this.imgSource = imgSource;
//    }

    public Poster() {
        // Empty Constructor
    }

    public Poster(String data) {
        this.data = data;
    }

    public Picture draw() {
        return imgSource;
    }

    public void updateImgSource(Picture imgSource) {
        this.imgSource = imgSource;
    }

    public Picture getImgSource() {
        return imgSource;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setImgSource(Picture imgSource) {
        this.imgSource = imgSource;
    }
}

