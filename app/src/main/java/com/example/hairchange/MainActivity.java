package com.example.hairchange;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_PERMISSION_CAMEAR = 100;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 101;

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_GALLERY = 1;

    private Button btnCamera;
    private Button btnFile;
    private Button btnLookBook;

    // temporary photo uri
    private Uri mImageCaptureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamera = findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted. So request the permission
                    permissionCheck_Camera();
                    return;
                }

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Create temporary file(user's camera capture)
                File dirCheck = new File(Environment.getExternalStorageDirectory(), MyUtil.combinePaths("HairChange", "pick_from_camera"));
                if (!dirCheck.exists())
                    dirCheck.mkdirs();

                String filename = "tmp_" + System.currentTimeMillis() + ".jpg";
                File cameraFile = new File(dirCheck.getPath(), filename);
                mImageCaptureUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.hairchange.fileprovider", cameraFile);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);
            }
        });

        btnFile = findViewById(R.id.btn_file);
        btnFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted. So request the permission
                    permissionCheck_WriteExternalStorage();
                    return;
                }

                // Call user's device gallery
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_GALLERY);
            }
        });

        btnLookBook = findViewById(R.id.btn_lookbook);
        btnLookBook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent resultViewIntent = new Intent(MainActivity.this, ResultActivity.class);
                startActivity(resultViewIntent);
            }
        });
    }

    private void permissionCheck_Camera() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_PERMISSION_CAMEAR);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
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
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied : WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "CANCEL", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == PICK_FROM_CAMERA) {
            // Already photo uri is in "mImageCaptureUri"
            cropImage(mImageCaptureUri);
        }
        else if (requestCode == PICK_FROM_GALLERY) {
            mImageCaptureUri = data.getData();
            cropImage(mImageCaptureUri);
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri resultUri = result.getUri();

            // go to photoview
            Intent photoViewIntent = new Intent(getApplication(), PhotoViewActivity.class);
            photoViewIntent.putExtra("PhotoUri", resultUri.toString());
            startActivity(photoViewIntent);
        }
    }

    private void cropImage(Uri inUri) {
        CropImage.activity(inUri)
                .setMinCropResultSize(1024,1024)
                .setMaxCropResultSize(1024,1024)
                .setAspectRatio(1,1)
                .start(this);
    }
}
