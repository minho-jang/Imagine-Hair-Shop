package com.example.hairchange;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static android.content.Context.ACTIVITY_SERVICE;

public class MyUtil {
    public final static String TAG = "MyUtil";
    private static final int ID_LENGTH = 20;

    public static String getRandId(Context context) {
        String randid = "";

        File file = new File(context.getFilesDir().getPath(), "randid.txt");
        FileReader fr = null;

        // 파일이 존재하면 읽어온다
        if (file.exists()) {
            Log.d(TAG, "File is existed.");
            int data;
            StringBuilder buf = new StringBuilder();
            try {
                fr = new FileReader(file);
                while ( (data=fr.read()) != -1) {
                    buf.append((char)data);
                }
                randid = buf.toString();
                fr.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 파일이 없으면 새로 만든다.
        else {
            Log.d(TAG, "File is not existed.");

            Random random = new Random();
            StringBuilder buf = new StringBuilder();
            for(int i = 0; i < ID_LENGTH; i++) {
                // random.nextBoolean() 는 랜덤으로 true, false 를 리턴.
                // true > 랜덤 한 소문자를
                if (random.nextBoolean()) {
                    buf.append((char)((int)(random.nextInt(26)) + 97));
                }
                // false > 랜덤 한 숫자를 StringBuffer 에 append 한다.
                else {
                    buf.append((random.nextInt(10)));
                }
            }
            randid = buf.toString();

            // Save random id
            FileWriter fw = null;
            try {
                fw = new FileWriter(file);
                fw.write(randid);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "randid : " + randid);
        return randid;
    }

    // input : Strings
    // output : File path consisted of input Strings
    public static String combinePaths(String ... paths) {
        if ( paths.length == 0)
            return "";

        File combined = new File(paths[0]);

        int i = 1;
        while ( i < paths.length ) {
            combined = new File(combined, paths[i]);
            ++i;
        }

        return combined.getPath();
    }

    // input : Image file path
    // output : Image file uri
    public static Uri getImageContentUri(Context context, String absPath) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { absPath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));
        } else if (!absPath.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, absPath);
            return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return null;
        }
    }
}
