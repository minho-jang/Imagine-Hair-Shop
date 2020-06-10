package com.example.hairchange;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<File> images;
    public GridAdapter() {
        images = new ArrayList<File>();
    }
    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public File getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext();
        ImageView image;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item, parent, false);

        }
        image = convertView.findViewById(R.id.image_item1);
        Bitmap bitmap = BitmapFactory.decodeFile(images.get(position).getAbsolutePath());
        image.setImageBitmap(bitmap);

        image.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO : click event
            }
        });

//        image.setImageResource(images.get(position));
        Log.i("convertView", "true");

        return convertView;
    }

    public void addItem(File item) {
        images.add(item);
    }

}
