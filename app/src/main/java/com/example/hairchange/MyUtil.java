package com.example.hairchange;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

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

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

//    public static String getPathFromUri(Context context, Uri uri){
//        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null );
//        cursor.moveToNext();
//        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
//        cursor.close();
//        return path;
//    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The activity.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPathFromUri(final Context context, final Uri uri) {

        // DocumentProvider
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }else{
                    Toast.makeText(context, "Could not get file path. Please try again", Toast.LENGTH_SHORT).show();
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    contentUri = MediaStore.Files.getContentUri("external");
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }
}
