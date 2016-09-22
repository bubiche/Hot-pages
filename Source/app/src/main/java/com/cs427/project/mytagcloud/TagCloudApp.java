package com.cs427.project.mytagcloud;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagCloudApp extends Application {
    private static TagCloudApp instance;

    public List<PageData> twitterData = null;
    public List<PageData> facebookData = null;
    public HashMap<String, List<Integer>> twitterTimeSeries = null;
    public HashMap<String, List<Integer>> facebookTimeSeries = null;
    public List<String> selectedItems = null;
    public boolean isTwitter = true;
    //public String dialogContent = "";

    public TagCloudApp() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        instance = this;


    }

    public static TagCloudApp getInstance() {
        return instance;
    }

    public synchronized void initData() {
        if(twitterData == null) {
            twitterData = new ArrayList<>();
        }

        if(facebookData == null) {
            facebookData = new ArrayList<>();
        }

        if(twitterTimeSeries == null) {
            twitterTimeSeries = new HashMap<String, List<Integer>>();
        }

        if(facebookTimeSeries == null) {
            facebookTimeSeries = new HashMap<String, List<Integer>>();
        }

        if(selectedItems == null) {
            selectedItems = new ArrayList<String>();
        }
    }
}
