package com.cs427.project.mytagcloud;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by npnguyen on 04/09/2016.
 */
public class PageDataTagsAdapter extends TagsAdapter {
    private List<PageData> dataSet = new ArrayList<>();

    public PageDataTagsAdapter(List<PageData> data) {
        dataSet.clear();
        dataSet = new ArrayList<>(data);
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public View getView(final Context context, final int position, ViewGroup parent) {
        TextView tv = new TextView(context);
        tv.setText(dataSet.get(position).pageName);
        tv.setGravity(Gravity.CENTER);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.e("Click", "Tag " + position + " clicked.");
                TagCloudApp.getInstance().selectedItems.clear();
                TagCloudApp.getInstance().selectedItems.add(dataSet.get(position).pageName);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                String message = "Page name: " + dataSet.get(position).pageName +"\n" +
                        "Number of fans: " + dataSet.get(position).fanCount;
                builder.setMessage(message)
                        .setPositiveButton(R.string.goToPage, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dataSet.get(position).pageURL));
                                context.startActivity(browserIntent);
                            }
                        })
                        .setNegativeButton(R.string.compare, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final List<CharSequence> items = new ArrayList<CharSequence>();
                                String chosenPageName = dataSet.get(position).pageName;
                                for(PageData curPage : dataSet) {
                                    if(!curPage.pageName.equals(chosenPageName)) {
                                        items.add(curPage.pageName);
                                    }
                                }
                                final AlertDialog.Builder choosePageDialog = new AlertDialog.Builder(context);
                                choosePageDialog.setTitle("Choose word(s) to compare with " + chosenPageName)
                                        .setMultiChoiceItems(items.toArray(new CharSequence[items.size()]), null, new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                if (isChecked) {
                                                    TagCloudApp.getInstance().selectedItems.add(items.get(which).toString());
                                                    //Log.d("CHECKBOX", items.get(which).toString());
                                                } else if (TagCloudApp.getInstance().selectedItems.contains(items.get(which).toString())) {
                                                    TagCloudApp.getInstance().selectedItems.remove(items.get(which).toString());
                                                }
                                            }
                                        })
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(context, GraphActivity.class);
                                                context.startActivity(intent);
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });

                                if(TagCloudApp.getInstance().isTwitter) {
                                    choosePageDialog.setIcon(R.drawable.twitter);
                                }
                                else {
                                    choosePageDialog.setIcon(R.drawable.fb);
                                }

                                AlertDialog myDialog = choosePageDialog.create();
                                myDialog.show();
                            }
                        })
                        .setTitle(R.string.pageInfo);

                if(TagCloudApp.getInstance().isTwitter) {
                    builder.setIcon(R.drawable.twitter);
                }
                else {
                    builder.setIcon(R.drawable.fb);
                }

                AlertDialog dialog = builder.create();


                dialog.show();
                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                textView.setTextSize(18);
                //textView.setTypeface(Typeface.DEFAULT_BOLD);
                //textView.setText(Html.fromHtml(message));
                //Toast.makeText(context, "Tag " + position + " clicked", Toast.LENGTH_SHORT).show();
            }
        });
        tv.setTextColor(Color.LTGRAY);
        return tv;
    }

    @Override
    public Object getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public int getPopularity(int position) {
        return dataSet.get(position).fanCount;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {
        view.setBackgroundColor(themeColor);
    }
}
