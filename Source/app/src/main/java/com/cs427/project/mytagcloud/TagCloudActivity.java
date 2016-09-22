package com.cs427.project.mytagcloud;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class TagCloudActivity extends AppCompatActivity {

    private static final String TWITTER_URL = "https://happy-root-8930.justapis.io/twitter";
    private static final String FACEBOOK_URL = "https://happy-root-8930.justapis.io/facebook";
    private static final String TWITTER_TIME_SERIES_URL = "https://happy-root-8930.justapis.io/twitter_6months";
    private static final String FACEBOOK_TIME_SERIES_URL = "https://happy-root-8930.justapis.io/facebook_6months";
    private AlertDialog dialog;
    private TagCloudView tagCloudView;
    private PageDataTagsAdapter adapterTwitter20;
    private PageDataTagsAdapter adapterTwitter10;
    private PageDataTagsAdapter adapterFb20;
    private PageDataTagsAdapter adapterFb10;
    private Drawable highlight;
    private ImageView twitterIcon;
    private ImageView fbIcon;
    private RadioButton radioTop10;
    private RadioButton radioTop20;
    //private boolean isTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_cloud);

        tagCloudView = (TagCloudView) findViewById(R.id.tag_cloud);
        tagCloudView.setBackgroundColor(Color.TRANSPARENT);
        highlight = ContextCompat.getDrawable(this, R.drawable.highlight);
        twitterIcon = (ImageView)findViewById(R.id.twitter_icon);
        fbIcon = (ImageView)findViewById(R.id.fb_icon);
        radioTop10 = (RadioButton)findViewById(R.id.radio_10);
        radioTop20 = (RadioButton)findViewById(R.id.radio_20);

        if(isNetworkAvailable()) {
            InitTask initTask = new InitTask(this);
            initTask.execute();
            twitterIcon.setBackground(highlight);
            radioTop20.setChecked(true);
            TagCloudApp.getInstance().isTwitter = true;
        }
        else {
            Toast.makeText(this, "This application requires internet connection!", Toast.LENGTH_LONG).show();
        }
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_10:
                if (checked) {
                    if (TagCloudApp.getInstance().isTwitter) {
                        tagCloudView.setAdapter(adapterTwitter10);
                    } else {
                        tagCloudView.setAdapter(adapterFb10);
                    }
                }
                    break;
            case R.id.radio_20:
                if (checked) {
                    if (TagCloudApp.getInstance().isTwitter) {
                        tagCloudView.setAdapter(adapterTwitter20);
                    } else {
                        tagCloudView.setAdapter(adapterFb20);
                    }
                }
                    break;
        }
    }

    public void onIconClicked(View view) {
        switch (view.getId()) {
            case R.id.twitter_icon:
                TagCloudApp.getInstance().isTwitter = true;
                fbIcon.setBackgroundResource(0);
                twitterIcon.setBackground(highlight);
                if(radioTop20.isChecked()) {
                    tagCloudView.setAdapter(adapterTwitter20);
                }
                else {
                    tagCloudView.setAdapter(adapterTwitter10);
                }
                break;
            case R.id.fb_icon:
                TagCloudApp.getInstance().isTwitter = false;
                twitterIcon.setBackgroundResource(0);
                fbIcon.setBackground(highlight);
                if(radioTop20.isChecked()) {
                    tagCloudView.setAdapter(adapterFb20);
                }
                else {
                    tagCloudView.setAdapter(adapterFb10);
                }
                break;
        }
    }

    private class InitTask extends AsyncTask<Void, Void, Integer> {
        private Context context;

        private JSONObject ttJSON;
        private JSONObject fbJSON;
        private JSONObject ttTSJSON;
        private JSONObject fbTSJSON;

        public InitTask(Context context) {
            this.context = context;
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Getting social media trend data...")
                    .setTitle("Please wait");
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            TagCloudApp.getInstance().initData();
            TagCloudApp.getInstance().twitterData.clear();
            TagCloudApp.getInstance().facebookData.clear();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                // REQUEST DATA FOR WORD CLOUD

                // request twitter data
                URL twitterURL = new URL(TWITTER_URL);
                HttpURLConnection twitterConn = (HttpURLConnection) twitterURL.openConnection();
                twitterConn.setReadTimeout(10000);
                twitterConn.setConnectTimeout(15000);
                twitterConn.setRequestMethod("POST");
                twitterConn.setDoInput(true);
                twitterConn.setDoOutput(true);

                // request facebook data
                URL facebookURL = new URL(FACEBOOK_URL);
                HttpURLConnection facebookConn = (HttpURLConnection) facebookURL.openConnection();
                facebookConn.setReadTimeout(10000);
                facebookConn.setConnectTimeout(15000);
                facebookConn.setRequestMethod("POST");
                facebookConn.setDoInput(true);
                facebookConn.setDoOutput(true);

                int twitterResponseCode = twitterConn.getResponseCode();

                if(twitterResponseCode == HttpsURLConnection.HTTP_OK) {
                    String raw = "";
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(twitterConn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        raw += line;
                    }
                    //Log.d("TWITTER", raw);
                    ttJSON = new JSONObject(raw);

                    Iterator<String> iter = ttJSON.keys();
                    while (iter.hasNext()) {
                        PageData pageData = new PageData();
                        String key = iter.next();
                        pageData.pageName = key;
                        JSONObject contentJSON = ttJSON.getJSONObject(key);
                        pageData.fanCount = contentJSON.getInt("fan");
                        pageData.pageURL = contentJSON.getString("url");
                        TagCloudApp.getInstance().twitterData.add(pageData);
                    }
                }
                else {
                    return -1;
                }

                int facebookResponseCode = facebookConn.getResponseCode();

                if(facebookResponseCode == HttpsURLConnection.HTTP_OK) {
                    String raw = "";
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(facebookConn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        raw += line;
                    }
                    //Log.d("FACEBOOK", raw);
                    fbJSON = new JSONObject(raw);

                    Iterator<String> iter = fbJSON.keys();
                    while (iter.hasNext()) {
                        PageData pageData = new PageData();
                        String key = iter.next();
                        pageData.pageName = key;
                        JSONObject contentJSON = fbJSON.getJSONObject(key);
                        pageData.fanCount = contentJSON.getInt("fan");
                        pageData.pageURL = contentJSON.getString("url");
                        TagCloudApp.getInstance().facebookData.add(pageData);
                    }
                }
                else {
                    return -2;
                }

                // REQUEST DATA FOR TIME SERIES

                // twitter
                URL twitterTSURL = new URL(TWITTER_TIME_SERIES_URL);
                HttpURLConnection twitterTSConn = (HttpURLConnection) twitterTSURL.openConnection();
                twitterTSConn.setReadTimeout(10000);
                twitterTSConn.setConnectTimeout(15000);
                twitterTSConn.setRequestMethod("POST");
                twitterTSConn.setDoInput(true);
                twitterTSConn.setDoOutput(true);

                // facebook
                URL facebookTSURL = new URL(FACEBOOK_TIME_SERIES_URL);
                HttpURLConnection facebookTSConn = (HttpURLConnection) facebookTSURL.openConnection();
                facebookTSConn.setReadTimeout(10000);
                facebookTSConn.setConnectTimeout(15000);
                facebookTSConn.setRequestMethod("POST");
                facebookTSConn.setDoInput(true);
                facebookTSConn.setDoOutput(true);

                int twitterTSResponseCode = twitterTSConn.getResponseCode();

                if(twitterTSResponseCode == HttpsURLConnection.HTTP_OK) {
                    String raw = "";
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(twitterTSConn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        raw += line;
                    }
                    ttTSJSON = new JSONObject(raw);

                    Iterator<String> iter = ttTSJSON.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        JSONArray contentJSON = ttTSJSON.getJSONArray(key);
                        List<Integer> fanCount = new ArrayList<Integer>();
                        for(int i = 0; i < contentJSON.length(); ++i) {
                            fanCount.add(contentJSON.getInt(i));
                        }
                        TagCloudApp.getInstance().twitterTimeSeries.put(key, fanCount);
                    }
                }
                else {
                    return -1;
                }

                int facebookTSResponseCode = facebookTSConn.getResponseCode();

                if(facebookTSResponseCode == HttpsURLConnection.HTTP_OK) {
                    String raw = "";
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(facebookTSConn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        raw += line;
                    }
                    fbTSJSON = new JSONObject(raw);

                    Iterator<String> iter = fbTSJSON.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        JSONArray contentJSON = fbTSJSON.getJSONArray(key);
                        List<Integer> fanCount = new ArrayList<Integer>();
                        for(int i = 0; i < contentJSON.length(); ++i) {
                            fanCount.add(contentJSON.getInt(i));
                        }
                        TagCloudApp.getInstance().facebookTimeSeries.put(key, fanCount);
                    }
                }
                else {
                    return -2;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(result == 0) {
                Collections.sort(TagCloudApp.getInstance().twitterData, new Comparator<PageData>() {
                    @Override
                    public int compare(PageData lhs, PageData rhs) {
                        return rhs.fanCount - lhs.fanCount;
                    }
                });

                Collections.sort(TagCloudApp.getInstance().facebookData, new Comparator<PageData>() {
                    @Override
                    public int compare(PageData lhs, PageData rhs) {
                        return rhs.fanCount - lhs.fanCount;
                    }
                });

                // initialize all adapters
                // since there are only 4 options, I'm doing this to improve run time a little bit
                adapterTwitter20 = new PageDataTagsAdapter(TagCloudApp.getInstance().twitterData);
                adapterTwitter10 = new PageDataTagsAdapter(TagCloudApp.getInstance().twitterData.subList(0, 10));
                adapterFb20 = new PageDataTagsAdapter(TagCloudApp.getInstance().facebookData);
                adapterFb10 = new PageDataTagsAdapter(TagCloudApp.getInstance().facebookData.subList(0, 10));

                tagCloudView.setAdapter(adapterTwitter20);
                Toast.makeText(context, "DONE!", Toast.LENGTH_SHORT).show();
            }
            else {
                if(result == -1) {
                    Toast.makeText(context, "SOMETHING WENT WRONG WHEN GETTING TWITTER DATA", Toast.LENGTH_SHORT).show();
                }
                else if(result == -2) {
                    Toast.makeText(context, "SOMETHING WENT WRONG WHEN GETTING FACEBOOK DATA", Toast.LENGTH_SHORT).show();
                }
            }

            dialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
