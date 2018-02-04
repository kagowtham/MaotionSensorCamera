package com.example.maotionsensorcamera;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    public int counter=0;
    SharedPreferences preferences;
    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences=getSharedPreferences("address",Context.MODE_PRIVATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startTimer();

        return START_STICKY;
    }
    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }


    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));

                getRequest();
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onDestroy() {
        stoptimertask();
        super.onDestroy();
    }
  synchronized   void getRequest(){

        String URL = preferences.getString("address","")+"Test";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
             synchronized public void onResponse(String response) {
                Log.i("VOLLEY", response);
                if(response.equals("1")){
                    CapPhoto capPhoto=new CapPhoto();
                    capPhoto.onCreate();
                    capPhoto.onStart();
                    try {
                        Thread.sleep(1000);
                        upload();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> m=new HashMap<>();
                m.put("status","req");
                m.put("key","abc123");
                return m;
            }
        };

        MyVolley.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }
    public void upload(){


        String url = preferences.getString("address","")+"Upload?key=abc123";
        File f=new File(getFilesDir(),"photo.jpeg");
        HashMap<String,String> map=new HashMap<>();
       // map.put("key","abc123");


        MultipartRequest mr = new MultipartRequest(url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("image res error",error.getLocalizedMessage());
            }
        },
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("image res",response);
                    }
                }, f, f.length(), null, map,
                "image", new MultipartRequest.MultipartProgressListener() {
            @Override
            public void transferred(long transfered, int progress) {
               Log.i("transfered",transfered+" "+progress);
            }
        });



        MyVolley.getInstance(getApplicationContext()).addToRequestQueue(mr);
    }
    public class CapPhoto {
        private SurfaceHolder surfaceHolder;
        private Camera mcamera;
        private Camera.Parameters parameters ;
        public void onCreate(){
            Log.d("CAM","start");
            if(Build.VERSION.SDK_INT>9){
                StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            };
            Thread myThread=null;
        }

        public void onStart(){

            if (Camera.getNumberOfCameras()>=2){
                mcamera=Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }
            if(Camera.getNumberOfCameras()<2){
                mcamera=Camera.open();
            }
            SurfaceView sv=new SurfaceView(getApplicationContext());
            try {
                mcamera.setPreviewDisplay(sv.getHolder());
                //   mcamera.getParameters();
                //  mcamera.setParameters(parameters);
                mcamera.startPreview();
                mcamera.takePicture(null,null,mCall);

            } catch (IOException e) {
                e.printStackTrace();
            }
            surfaceHolder=sv.getHolder();
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        }
        Camera.PictureCallback mCall=new Camera.PictureCallback(){

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outputStream=null;

                try{
                    outputStream=openFileOutput("photo.jpeg", Context.MODE_PRIVATE);
                    outputStream.write(data);
                    outputStream.close();
                    Log.i("CAM", data.length + "byte written to:"+"photo.jpeg");
                    camkapa(surfaceHolder);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };



        public void camkapa(SurfaceHolder surfaceHolder){
            if (null==mcamera)
                return;
            mcamera.stopPreview();
            mcamera.release();
            mcamera=null;
            Log.i("CAM","Closed");

        }
    }


}
