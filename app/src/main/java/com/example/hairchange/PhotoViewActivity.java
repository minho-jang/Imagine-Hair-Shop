package com.example.hairchange;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MenuItem;
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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PhotoViewActivity extends AppCompatActivity {
    private static final String TAG = "PhotoViewActivity";

    private RequestQueue queue;
    private ProgressDialog progressDialog;
    private Bitmap originImage;

    private ImageItemAdapter adapter;
    private RecyclerView recyclerView;
    private ImageView photo;
    private ImageView sticker;
    private Button man;
    private Button woman;
    private Button theOthers;
    private BottomNavigationView bottomNavigationView;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);

        photo = findViewById(R.id.photo);
        sticker = findViewById(R.id.sticker);

        String photoUri = getIntent().getExtras().getString("PhotoUri");
        getImageFromURI(photoUri);  // uri 로 부터 이미지가져오기

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

        // minho {
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);

        bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_send: {

                        Bitmap background = ((BitmapDrawable) photo.getDrawable()).getBitmap();;
                        Bitmap hair = ((BitmapDrawable) sticker.getDrawable()).getBitmap();
                        Rect hairRect = new Rect();
                        sticker.getHitRect(hairRect);

                        Bitmap bitmapOverlay = Bitmap.createBitmap(background.getWidth(), background.getHeight(), background.getConfig());
                        Canvas canvas = new Canvas(bitmapOverlay);
                        canvas.drawBitmap(background, new Matrix(), null);
                        canvas.drawBitmap(hair, null, hairRect, null);

                        photo.setImageBitmap(bitmapOverlay);

                        String imageId = IMAGE_ID;
                        String url = SERVER_BASE_URL + imageId;
                        httpPostReqeust(url, bitmapOverlay);
                        return true;
                    }
                }
                return false;
            }
        });
        // minho }
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
        photoUri = photoUri.replace("file://", "");
        File imgFile = new File(photoUri);
        if (imgFile.exists()) {
            originImage = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Log.d(TAG, originImage.toString());
            Log.d(TAG, imgFile.getAbsolutePath());
            Log.d(TAG, photo.toString());
            photo.setImageBitmap(originImage);
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

    // minho {
    protected void httpPostReqeust(String url, Bitmap bitmap) {
        loading("Image uploading...");

        // Converting image to base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);

                        // response to Bitmap
                        final String resultString = response;
                        byte[] imageBytes = Base64.decode(resultString, 0);
                        Bitmap resultBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                        Toast.makeText(getApplicationContext(), "Image upload success", Toast.LENGTH_SHORT).show();
                        loadingEnd();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Image upload error", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                loadingEnd();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("image", imageString);
                return params;
            }
        };

//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
//                300000,         // 5 min
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void loading(final String msg) {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(PhotoViewActivity.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage(msg);
                        progressDialog.show();
                    }
                }, 0);
    }

    private void loadingEnd() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 0);
    }
    // minho }
}
