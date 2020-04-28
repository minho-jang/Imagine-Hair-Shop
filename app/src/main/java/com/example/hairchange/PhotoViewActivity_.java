package com.example.hairchange;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class PhotoViewActivity_ extends AppCompatActivity {
    private static final String TAG = "PhotoViewActivity";
    private static final int CROP_IMAGE_REQUEST_CODE = 1;
    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1;

    private ImageItemAdapter adapter;
    private RecyclerView recyclerView;
    private Button man;
    private Button woman;
    private Button theOthers;
    private ImageView mPhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);
        Log.d(TAG, "onCreate...");

        // get Intent data
        String photoUri = getIntent().getExtras().getString("PhotoUri");

        // Crop Intent
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(Uri.parse(photoUri.replace("file:", "")), "image/*");
        cropIntent.putExtra("outputX", 1024);
        cropIntent.putExtra("outputY", 1024);
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("return-data", true);
        startActivityForResult(cropIntent, CROP_IMAGE_REQUEST_CODE);

        mPhotoImageView = (ImageView)findViewById(R.id.photo);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == CROP_IMAGE_REQUEST_CODE) {
            final Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                mPhotoImageView.setImageBitmap(photo);
            }
        }
    }

    private void getImageFromURI(String photoUri) {
        ImageView photo = (ImageView)findViewById(R.id.photo);
        photoUri = photoUri.replace("file:", "");
        File imgFile = new File(photoUri);
        Log.i("imgFile", imgFile.toString());
        if(imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Log.i("abPath", bitmap.toString());

            // 이미지 너비, 높이 같게 보여주고, 90도 회전 및 좌우 반전.
            Log.d(TAG, "bitmap width: " + bitmap.getWidth() + " bitmap height: " + bitmap.getHeight());
            int edge = bitmap.getWidth() > bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth();
            Matrix matrix = new Matrix();
            matrix.preRotate(90);
            matrix.preScale(-1, 1);
            bitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth()-bitmap.getHeight(), 0, edge, edge, matrix, true);

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
