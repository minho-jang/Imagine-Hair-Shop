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
import java.util.Arrays;

/**
 * 룩북을 보여주는 Fragment
 */
public class LookBookFragment extends Fragment {
    public LookBookFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lookbook, container, false);

        GridAdapter adapter = new GridAdapter();

        File dirResult = new File(MyUtil.combinePaths(v.getContext().getFilesDir().getPath(), "results"));
        File[] files = dirResult.listFiles();
        if (files != null) {
            Arrays.sort(files);

            if (files.length != 0){
                for (File file : files) {
                    adapter.addItem(file);
                }
            }
        }

        GridView grid = (GridView)v.findViewById(R.id.gridview);
        grid.setAdapter(adapter);

        return v;
    }
}
