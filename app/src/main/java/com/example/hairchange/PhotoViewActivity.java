package com.example.hairchange;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

    private String imageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        photo = findViewById(R.id.photo);
        sticker = findViewById(R.id.sticker);

        String photoPath = getIntent().getExtras().getString("PhotoPath");
        setBitmapFromUri(photoPath);  // PhotoPath 에서 이미지 불러오기

        man = findViewById(R.id.man);
        woman = findViewById(R.id.woman);
        theOthers = findViewById(R.id.the_others);
        man.setSelected(true);

        // RecyclerView
        recyclerView = findViewById(R.id.recycler1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Set Adapter
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
                Log.i("action", "onScroll: x: " + sticker.getX() + " y: " + sticker.getY());
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
                        Bitmap background = ((BitmapDrawable) photo.getDrawable()).getBitmap();
                        Bitmap hair = ((BitmapDrawable) sticker.getDrawable()).getBitmap();
                        Rect hairRect = new Rect();
                        sticker.getHitRect(hairRect);

                        Bitmap bitmapOverlay = Bitmap.createBitmap(background.getWidth(), background.getHeight(), background.getConfig());
                        Canvas canvas = new Canvas(bitmapOverlay);
                        canvas.drawBitmap(background, new Matrix(), null);
                        canvas.drawBitmap(hair, null, hairRect, null);

                        // ISSUE 약간 sticker가 오른쪽 으로 감 (only woman hair)
//                        photo.setImageBitmap(bitmapOverlay);

                        // HTTP post request - image upload
                        imageId = MyUtil.getRandId(getApplicationContext());
                        String url = SERVER_BASE_URL + "start/" + imageId;
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
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
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

    private void setBitmapFromUri(String photoPath) {
        Log.d(TAG, "photoPath : " + photoPath);

        File imgFile = new File(photoPath);
        if (imgFile.exists()) {
            originImage = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            photo.setImageBitmap(originImage);
        } else {
            Log.d(TAG, "[" + photoPath + "] is not exited...");
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

        // Converting bitmap image to base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // Request : the image upload
        // Response: just OK.
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "response: " + response);

                        Toast.makeText(getApplicationContext(), "Image upload success", Toast.LENGTH_SHORT).show();
                        loadingEnd();

                        // 이미지가 업로드 되면
                        // 서버는 바로 이미지 처리를 시작하며
                        // 안드로이드는 서비스(백그라운드) 형태로 처리가 끝났는지 확인한다.
                        Intent intentService = new Intent(PhotoViewActivity.this, ImageProcessCheckService.class);
                        intentService.putExtra("imageId", imageId);
                        startService(intentService);

                        // 메인화면으로 이동
                        Intent intentMain = new Intent(PhotoViewActivity.this, MainActivity.class);
                        intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    // 기존 백스택에 MainActivity가 있으면 그걸 가져온다.
                        startActivity(intentMain);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingEnd();

                Toast.makeText(getApplicationContext(), "Image upload error", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("image", imageString);
                return params;
            }
        };

        // wait 1 mins for response
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,         // 1 min
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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
