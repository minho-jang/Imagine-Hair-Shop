package com.example.hairchange;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnCamera = findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 명시적 인텐트
                Intent cameraIntent = new Intent(getApplication(), CameraActivity.class);
                startActivity(cameraIntent);
            }
        });

        final Button btnFile = findViewById(R.id.btn_file);
        btnFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 미구현
                Toast.makeText(getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
