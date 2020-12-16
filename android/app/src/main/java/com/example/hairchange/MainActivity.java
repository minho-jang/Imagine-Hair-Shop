package com.example.hairchange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 메인 화면 액티비티.
 * 기본적으로 가이드를 보여주고, 룩북을 클릭하면 본인이 시도했었던 사진을 볼 수 있다.
 * Floating Button '+'을 통해 사진을 선택한다.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_PERMISSION_CAMEAR = 100;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 101;

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_GALLERY = 1;

    private static final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private RequestQueue queue;
    private ProgressDialog progressDialog;

    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private BottomNavigationView bottomNavigationView;

    // temporary photo uri
    private Uri mImageCaptureUri;
    GuideFragment guideFragment;
    LookBookFragment lookBookFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);   // HTTP Request Queue

        guideFragment = new GuideFragment();
        lookBookFragment = new LookBookFragment();

        // 처음 메인화면 기본값은 가이드 Fragment
        if(findViewById(R.id.control) != null) {
            if(savedInstanceState != null)
                return;

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.control, guideFragment).commit();
        }

        // Floating Button Setting {
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);

        FloatingActionButton.OnClickListener onClickListener = new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch(id) {
                    case R.id.fab:
                        anim();
                        break;
                    case R.id.fab1:
                        anim();
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            // Permission is not granted. So request the permission
                            permissionCheck_Camera();
                            return;
                        }

                        callCamera();
                        break;
                    case R.id.fab2:
                        anim();
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Permission is not granted. So request the permission
                            permissionCheck_WriteExternalStorage();
                            return;
                        }

                        callGallery();
                        break;
                }
            }

            public void anim() {
                if(isFabOpen) {
                    fab1.startAnimation(fab_close);
                    fab2.startAnimation(fab_close);
                    fab1.setClickable(false);
                    fab2.setClickable(false);
                    isFabOpen = false;
                } else {
                    fab1.startAnimation(fab_open);
                    fab2.startAnimation(fab_open);
                    fab1.setClickable(true);
                    fab2.setClickable(true);
                    isFabOpen = true;
                }
            }
        };

        fab.setOnClickListener(onClickListener);
        fab1.setOnClickListener(onClickListener);
        fab2.setOnClickListener(onClickListener);
        // } Floating Button Setting

        // Bottom Navigation Setting {
        bottomNavigationView = findViewById(R.id.contents_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (item.getItemId()) {
                    case R.id.action_one:
                        transaction.replace(R.id.control, guideFragment);
                        transaction.commit();
                        break;

                    case R.id.action_three:
                        transaction.replace(R.id.control, lookBookFragment);
                        transaction.commit();
                        break;
                }
                return true;
            }
        });
        // } Bottom Navigation Setting
    }

    // Call user's device Gallery
    private void callGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_GALLERY);
    }

    // Call user's device Camera
    private void callCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create temporary file to hold user's image
        String url = System.currentTimeMillis() + "_hairchange.jpg";
        mImageCaptureUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.hairchange.fileprovider", new File(Environment.getExternalStorageDirectory(), url));
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    private void permissionCheck_Camera() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_CAMEAR);
    }

    private void permissionCheck_WriteExternalStorage() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMEAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the contacts-related task you need to do.
                    callCamera();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied : CAMERA", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the contacts-related task you need to do.
                    callGallery();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied : WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    /**
     * 카메라 또는 갤러리에 접근 후 결과 처리.
     * 요청코드를 비교하여 적절한 코드를 실행한다.
     * 카메라 또는 갤러리로부터 온 사진은 자르기 위해 서버에 전송한다.
     * 크롭되어 온 사진은 다음 액티비티로 전달한다.
     *
     * @param requestCode   요청 코드
     * @param resultCode    결과 코드
     * @param data          결과 데이터
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 요청 코드를 비교하기 전, 사용자가 취소할 경우 또는 결과에 있어서 에러가 있는 경우
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "CANCEL", Toast.LENGTH_SHORT).show();
            return;
        }

        // 카메라 요청에 대한 응답이 왔으면
        if (requestCode == PICK_FROM_CAMERA) {
            // Already photo uri is in "mImageCaptureUri"
            cropImage(mImageCaptureUri);
        }
        // 갤러리 요청에 대한 응답이 왔으면
        else if (requestCode == PICK_FROM_GALLERY) {
        mImageCaptureUri = data.getData();
        cropImage(mImageCaptureUri);
    }
}

    /**
     * 이미지를 자르기 위해 이미지 서버로 전송.
     * AI 기술이 적용되기 위해 input 형식을 맞추는 작업.
     *
     * @param imageFile
     */
    protected void cropImage(Uri imageFile) {
        loading("Image cropping...");

        Log.d(TAG, "imageFile : " + imageFile.getPath());
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (bitmap == null) {
            Log.d(TAG, "bitmap is null...");
            return;
        }

        // 이미지 회전 이슈 해결 {
        ExifInterface ei = null;
        try {
            InputStream is = getContentResolver().openInputStream(imageFile);
            assert is != null;
            ei = new ExifInterface(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert ei != null;
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = MyUtil.rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = MyUtil.rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = MyUtil.rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        // } 이미지 회전 이슈 해결

        // HTTP POST request to Server
        String imageId = MyUtil.getRandId(getApplicationContext());
        String url = SERVER_BASE_URL + "crop/" + imageId;

        // Converting bitmap image to base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // Request : image
        // Response: cropped image
        Log.d(TAG, "Crop Request Start");
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "response: " + response);
                        loadingEnd();
                        if ("0".equals(response)) {
                            Toast.makeText(getApplicationContext(), "Image Crop Error: Photo Error", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Image Crop success", Toast.LENGTH_SHORT).show();
                            File cropFile = new File(base64ToFile(response));

                            // go to photoview
                            Intent photoViewIntent = new Intent(getApplication(), PhotoViewActivity.class);
                            photoViewIntent.putExtra("PhotoPath", cropFile.getAbsolutePath());
                            startActivity(photoViewIntent);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingEnd();

                Toast.makeText(getApplicationContext(), "Image Crop Error: Server Error", Toast.LENGTH_SHORT).show();
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

    // input : base64 string
    // output: image file name (path)
    private String base64ToFile(String string) {
        // string to byte array
        final String resultString = string;
        byte[] imageBytes = Base64.decode(resultString, 0);

        // Byte array to jpg file
        long now = System.currentTimeMillis();
        File reusltFile = new File(getFilesDir().getPath(), now + "_tmp.jpg");
        Log.d(TAG, "reusltFile :" + reusltFile.getPath());

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(reusltFile, false);
            outputStream.write(imageBytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reusltFile.getPath();
    }

    private void loading(final String msg) {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage(msg);
                        progressDialog.setCancelable(false);
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

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한 번 더 누르면 종료합니다", Toast.LENGTH_SHORT).show();
        }
    }
}
