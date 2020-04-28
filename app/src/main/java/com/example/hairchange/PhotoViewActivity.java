package com.example.hairchange;

import android.Manifest;
import android.content.Intent;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;

public class PhotoViewActivity extends AppCompatActivity {
    private static final String TAG = "PhotoViewActivity";

    private ImageItemAdapter adapter;
    private RecyclerView recyclerView;
    private ImageView sticker;
    private Button man;
    private Button woman;
    private Button theOthers;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);

        // get Intent data
        String photoUri = getIntent().getExtras().getString("PhotoUri");
        Log.d(TAG, "photoUri : " + photoUri);

        getImageFromURI(photoUri);  // uri 로 부터 이미지가져오기

        // Set Hair style
        man = findViewById(R.id.man);
        woman = findViewById(R.id.woman);
        theOthers = findViewById(R.id.the_others);
        man.setSelected(true);

        recyclerView = findViewById(R.id.recycler1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapter = new ImageItemAdapter();

        adapter.addItem(R.drawable.man_raised1);
        adapter.addItem(R.drawable.man_raised2);

        recyclerView.setAdapter(adapter);

        sticker = findViewById(R.id.sticker);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                float viewportOffsetX = distanceX * frame.getWidth() / sticker.getWidth();
//                float viewportOffsetY = -distanceY * frame.getHeight() / sticker.getHeight();
  //              sticker.setX(sticker.getLeft() + viewportOffsetX);
//                sticker.setY(sticker.getBottom() + viewportOffsetY);
                if(mScaleGestureDetector.isInProgress()) {
                    return false;
                }
                sticker.setX(sticker.getX() + e2.getX() - e1.getX());
                sticker.setY(sticker.getY() + e2.getY() - e1.getY());
                Log.i("action", "onScroll");
                return true;
            }
        };
        final GestureDetector gestureDetector = new GestureDetector(this, mGestureListener);
        sticker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean retVal = mScaleGestureDetector.onTouchEvent(event);
                retVal = gestureDetector.onTouchEvent(event) || retVal;
                return retVal || PhotoViewActivity.super.onTouchEvent(event);
//                mScaleGestureDetector.onTouchEvent(event);
//                gestureDetector.onTouchEvent(event);
//                return true;
            }
        });



        View.OnTouchListener mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i("action", "down");
                }
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    v.setX(v.getX() + (event.getX()) - (v.getWidth()/2));
                    v.setY(v.getY() + (event.getY()) - (v.getHeight()/2));
                    Log.i("action", "move");

                }
                return true;
            }
        };
        //sticker.setOnTouchListener(mTouchListener);

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        mScaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            sticker.setScaleX(mScaleFactor);
            sticker.setScaleY(mScaleFactor);
            return true;
        }
    }

    private void getImageFromURI(String photoUri) {
        ImageView photo = (ImageView)findViewById(R.id.photo);
        photoUri = photoUri.replace("file://", "");
        File imgFile = new File(photoUri);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Log.i("abPath", bitmap.toString());
            photo.setImageBitmap(bitmap);
        }


    }

    public void btnClick(View view) {
        adapter = new ImageItemAdapter();
        view.setSelected(true);
        if(view.getId() == R.id.man) {
            woman.setSelected(false);
            theOthers.setSelected(false);

            adapter.addItem(R.drawable.man_raised1);
            adapter.addItem(R.drawable.man_raised2);
        } else if (view.getId() == R.id.woman) {
            man.setSelected(false);
            theOthers.setSelected(false);

            adapter.addItem(R.drawable.woman_blond_long);
        } else if (view.getId() == R.id.the_others) {
            man.setSelected(false);
            woman.setSelected(false);

            adapter.addItem(R.drawable.man_raised2);
            adapter.addItem(R.drawable.man_raised2);
            adapter.addItem(R.drawable.man_raised2);
            adapter.addItem(R.drawable.man_raised2);
            adapter.addItem(R.drawable.man_raised2);
            adapter.addItem(R.drawable.man_raised2);
            adapter.addItem(R.drawable.man_raised2);
            adapter.addItem(R.drawable.man_raised2);
        }

        recyclerView.setAdapter(adapter);
    }


}
