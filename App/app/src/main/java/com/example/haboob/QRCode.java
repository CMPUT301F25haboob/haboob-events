package com.example.haboob;

public class QRCode {
    // TODO: Need to figure out what the fuck this does
    private String data;

    public QRCode() {
        // Empty Constructor
    }
    public QRCode(String data) {
        this.data = data;
    }
//    public EventFragment showEventFragment() {
//         Something like this maybe?
//  }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
