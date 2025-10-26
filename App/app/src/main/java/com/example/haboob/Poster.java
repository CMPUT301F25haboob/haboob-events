package com.example.haboob;

import android.graphics.Picture;

// Not sure exactly what Picture does, maybe we can use this though?
public class Poster {
    private Picture imgSource;

    public Poster(Picture imgSource) {
        this.imgSource = imgSource;
    }

    public Picture draw() {
        return imgSource;
    }

    public void updateImgSource(Picture imgSource) {
        this.imgSource = imgSource;
    }
}
