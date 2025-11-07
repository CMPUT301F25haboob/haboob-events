package com.example.haboob.ui.notifications;

import com.example.haboob.Event;

import org.w3c.dom.Text;

public class Notification {
    private Event event; // This is the event that the notification is related to
                         // Clicking the notification will allow you to view the event fragment
    private String content;

    public Notification(Event event, String content)
    {
        this.event = event;
        this.content = content;
    }
}
