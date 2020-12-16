package com.example.hairchange;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

import java.io.File;

public class LookBookFragment extends Fragment {
    public LookBookFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lookbook, container, false);

        GridAdapter adapter = new GridAdapter();

        File dirResult = new File(MyUtil.combinePaths(v.getContext().getFilesDir().getPath(), "results"));
        File[] files = dirResult.listFiles();

        if (files != null && files.length != 0){
            for(int i = 0; i < files.length; i++) {
                adapter.addItem(files[i]);
            }
//            Log.d(TAG, "file count : " +files.length);
//            Log.d(TAG, "absPath : " + file.getAbsolutePath());

        }



//        Log.i("getItem", adapter.getItem(0)+"");
//        Log.i("getItem", adapter.getItem(1)+"");
        GridView grid = (GridView)v.findViewById(R.id.gridview);
        grid.setAdapter(adapter);

        return v;
    }
}
