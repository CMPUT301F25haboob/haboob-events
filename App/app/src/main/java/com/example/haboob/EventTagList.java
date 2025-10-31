package com.example.haboob;

import java.util.ArrayList;
import java.util.List;

public class EventTagList {
    private List<String> tagList;

    public  EventTagList() {}

    public EventTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }
}
