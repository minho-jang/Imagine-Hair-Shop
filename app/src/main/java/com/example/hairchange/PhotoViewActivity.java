package com.example.hairchange;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class PhotoViewActivity extends AppCompatActivity {
    private static final String TAG = "PhotoViewActivity";

    private ImageItemAdapter adapter;
    private RecyclerView recyclerView;
    private Button man;
    private Button woman;
    private Button theOthers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);

        String photoUri = getIntent().getExtras().getString("PhotoUri");
        // URI (Uniform Resource Identifier). 슬래시('/') 차이나는 이유는 모르겠네
        // gallery에서 선택으로 오면 "content://..."
        // 직접 찍어서 오면 "file:/..."
        getImageFromURI(photoUri);  // uri 로 부터 이미지가져오기
        Log.d(TAG, photoUri);
        Toast.makeText(getApplicationContext(), photoUri, Toast.LENGTH_LONG).show();     // 임시 출력.



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

    private void getImageFromURI(String photoUri) {
        ImageView photo = (ImageView)findViewById(R.id.photo);
        photoUri = photoUri.replace("file:", "");
        File imgFile = new File(photoUri);
        Log.i("imgFile", imgFile.toString());
        if(imgFile.exists()) {
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
