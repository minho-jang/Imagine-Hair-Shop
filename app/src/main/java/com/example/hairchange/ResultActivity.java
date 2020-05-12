package com.example.hairchange;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;

public class ResultActivity extends AppCompatActivity {

    private ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        photo = findViewById(R.id.photo);

        String imageFileName = getIntent().getStringExtra("imageFileName");
        String path = Environment.getExternalStorageDirectory() + imageFileName;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        photo.setImageBitmap(bitmap);
    }
}
