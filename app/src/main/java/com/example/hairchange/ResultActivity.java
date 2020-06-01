package com.example.hairchange;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ResultActivity extends AppCompatActivity {
    private static final String TAG = "ResultActivity";

    private ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String test = MyUtil.getRandId(getApplicationContext());
        Log.d(TAG, "id test : " + test);

        photo = findViewById(R.id.photo);

        File dirResult = new File(MyUtil.combinePaths(getFilesDir().getPath(), "results"));
        File[] files = dirResult.listFiles();
        File file;
        if (files != null && files.length != 0){
            file = files[0];
            Log.d(TAG, "file count : " +files.length);
            Log.d(TAG, "absPath : " + file.getAbsolutePath());
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            photo.setImageBitmap(bitmap);
        } else {
            Log.d(TAG, "Error. Can not find files");

            // 메인화면으로 이동
            Intent intentMain = new Intent(ResultActivity.this, MainActivity.class);
            intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    // 기존 백스택에 MainActivity가 있으면 그걸 가져온다.
            startActivity(intentMain);
        }
    }
}
