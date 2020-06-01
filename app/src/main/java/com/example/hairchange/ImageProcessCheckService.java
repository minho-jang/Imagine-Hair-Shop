package com.example.hairchange;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 이미지 처리를 위해 HTTP 요청을 보내고
 * response를 받아서 결과 사진을 주는 서비스
 */
public class ImageProcessCheckService extends IntentService {
    private static final String TAG = "ImageProcessCheckService";

    private Handler mHandler;
    private RequestQueue queue;
    private int checkCount;
    private boolean isProcessed;
    private String resultBase64;
    private String resultFilePath;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public ImageProcessCheckService() {
        super("ImageProcessCheckService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        queue = Volley.newRequestQueue(this);
        checkCount = 0;
        isProcessed = false;
        resultBase64 = "";
        resultFilePath = "";
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Service start : onHandleIntent()");

        String imageId = intent.getExtras().getString("imageId");

        // 1. loop check the image processing is end
        // 2. If yes, convert the response(base64) to file
        // 3. And notice to user (Toast)

        long start = System.currentTimeMillis();
        long end;
        while ( !isProcessed ) {
            if (checkCount > 20) {      // 20 이상이면 약 5분 소요이므로 중지.
                end = System.currentTimeMillis();
                Log.d(TAG, "Image processing take too long\n\ttime : " + (end-start)/1000.0);
                break;
            }

            checkCount++;
            Log.d(TAG, "checkCount : " + checkCount);

            String url = SERVER_BASE_URL + "result/" + imageId;
            httpGetReqeust(url);

            // Sleep
            long millis;
            if (checkCount >= 1 && checkCount <= 5)
                millis = 30000;        // 30 sec
            else
                millis = 10000;        // 10 sec

            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }

        if (isProcessed && resultBase64.length() != 0) {
            end = System.currentTimeMillis();
            Log.d(TAG, "Image processing time : " + (end-start)/1000.0);

            // save the result image
            resultFilePath = base64ToFile(resultBase64);
            Log.d(TAG, "Result image save complete. " + resultFilePath);

            // Main thread에 Toast를 실행시키도록 전달
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ImageProcessCheckService.this, "Image processing COMPLETE : " + resultFilePath, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.d(TAG, "Something broken !");
        }
    }

    // input : base64 string
    // output: image file name (path)
    private String base64ToFile(String string) {
        // string to byte array
        final String resultString = string;
        byte[] imageBytes = Base64.decode(resultString, 0);

        // Byte array to png file
        File dirCheck = new File(MyUtil.combinePaths(getFilesDir().getPath(), "results"));
        Log.d(TAG, "dirCheck :" + dirCheck);
        if (!dirCheck.exists())     // create "results" directory
            dirCheck.mkdirs();

        long now = System.currentTimeMillis();
        File resultFile = new File(dirCheck, now + "_result.png");
        Log.d(TAG, "resultFile :" + resultFile.getPath());
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(resultFile, false);
            outputStream.write(imageBytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultFile.getPath();
    }


    // Request : is the image processing end?
    // Response: result image string encoded Base64 or No
    protected void httpGetReqeust(String url) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "response: " + response);
                        if ("0".equals(response)) {
                            Log.d(TAG, "The result is not ready yet.");
                        } else {
                            isProcessed = true;
                            resultBase64 = response;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
