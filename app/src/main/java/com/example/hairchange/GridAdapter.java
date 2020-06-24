package com.example.hairchange;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;

/**
 * LookBook 그리드(grid) 레이아웃 어댑터.
 * 각 아이템(image) 별로 clickListener 추가.
 */
public class GridAdapter extends BaseAdapter {
    public final static String TAG = "GridAdapter";

    private ArrayList<File> images;
    public GridAdapter() {
        images = new ArrayList<File>();
    }
    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public File getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        final ImageView image;
        final File imageFile = images.get(position);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item, parent, false);

        }
        image = convertView.findViewById(R.id.image_item1);
        final Bitmap bitmap = BitmapFactory.decodeFile(images.get(position).getAbsolutePath());
        image.setImageBitmap(bitmap);

        image.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ImageView AlertDialog.
                ImageView iv = new ImageView(context);
                iv.setImageBitmap(bitmap);

                /**
                 * 이미지 크게 보여주는 AlertDialog.
                 */
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_DayNight_Dialog);
                alertBuilder.setView(iv);
                alertBuilder.setNegativeButton("OOPS!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDialog(context, imageFile);
                    }
                });
                alertBuilder.setPositiveButton("GOOD!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();

                Button btnNegative = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                btnNegative.setTextColor(Color.parseColor("#FF2626"));

                Button btnPositive = alert.getButton(DialogInterface.BUTTON_POSITIVE);

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                layoutParams.weight = 10;
                btnPositive.setLayoutParams(layoutParams);
                btnNegative.setLayoutParams(layoutParams);
            }
        });

//        image.setImageResource(images.get(position));
        Log.i("convertView", "true");

        return convertView;
    }

    public void addItem(File item) {
        images.add(item);
    }

    private void deleteDialog(Context context, final File f) {
        Log.d(TAG, "deleteDialog start");

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_DayNight_Dialog);
        builder.setMessage("삭제하시겠습니까?");
        builder.setNegativeButton("YEAH", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 파일 삭제.
                Log.d(TAG, "삭제될 파일 : " + f.getAbsolutePath());
                if (f.delete())
                    Log.d(TAG, "파일 삭제 완료");
                else
                    Log.d(TAG, "파일 삭제 실패");

                // 화면 새로고침.
                notifyDataSetChanged();
            }
        });
        builder.setPositiveButton("NOPE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

        Button btnNegative = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegative.setTextColor(Color.parseColor("#FF2626"));

        Button btnPositive = alert.getButton(DialogInterface.BUTTON_POSITIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }
}
