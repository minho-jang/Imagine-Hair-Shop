package com.example.hairchange;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PhotoViewActivity extends AppCompatActivity {
    private static final String TAG = "PhotoViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);

        String photoUri = getIntent().getExtras().getString("PhotoUri");
        // URI (Uniform Resource Identifier). 슬래시('/') 차이나는 이유는 모르겠네
        // gallery에서 선택으로 오면 "content://..."
        // 직접 찍어서 오면 "file:/..."
        Log.d(TAG, photoUri);
        Toast.makeText(getApplicationContext(), photoUri, Toast.LENGTH_LONG).show();     // 임시 출력.
    }
}
