package com.storage.app.flashlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;


public class Main extends Activity {
    private Context _context;
    private boolean _hasFlash;
    private boolean _isFlashOn;
    private static Camera _cam;
    private static Camera.Parameters _params;
    private ImageButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (ImageButton) findViewById(R.id.button);

        Log.e(FlahslightApplication.TAG, "before has camera:" + _hasFlash);
        _context = getApplicationContext();
        checkFlashExistance();
        Log.e(FlahslightApplication.TAG, "after has camera:" + _hasFlash);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_isFlashOn){
                    turnOff();
                    btn.setBackgroundColor(Color.argb(255,0,0,0));
                } else {
                    turnOn();
                    btn.setBackgroundColor(Color.argb(255,255,255,255));
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        getCamera();
    }

    private void checkFlashExistance(){
        _hasFlash = _context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if(!_hasFlash) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
            builder.setTitle("Error");
            builder.setMessage("Sorry, your device doesn't support flash light!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
    }

    private void getCamera(){
        if(_cam == null){
            try{
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                int cameraCount = Camera.getNumberOfCameras();
                Log.e(FlahslightApplication.TAG, "Camera count:" + cameraCount);
                for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                    Camera.getCameraInfo(camIdx, cameraInfo);
                    Log.e(FlahslightApplication.TAG, "cameraInfo.facing:" + cameraInfo.facing);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        Log.e(FlahslightApplication.TAG, ">>current id:" + cameraInfo.facing);
                        try {
                            _cam = Camera.open(camIdx);
                            _params = _cam.getParameters();
                            break;
                        } catch (RuntimeException e) {
                            Log.e(FlahslightApplication.TAG, "Camera failed to open:" + e.getLocalizedMessage());
                        }
                    }
                    Log.e(FlahslightApplication.TAG, "cameraInfo.facing:" + cameraInfo.facing);
                }
            }catch (RuntimeException e) {
                Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
            }
        }
    }

    //test
    private void turnOn(){
        if(!_isFlashOn) {
            if(_cam == null || _params == null){
                return;
            }

            _params = _cam.getParameters();
            _cam.startPreview();
////        _params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            if(_params.getFlashMode() != null) {
                _params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                Log.e(FlahslightApplication.TAG, "flash not null");
            }
            _cam.setParameters(_params);
            try {
                _cam.setPreviewTexture(new SurfaceTexture(0));
            } catch (IOException e){
                Log.e(FlahslightApplication.TAG, "IOException");
            }

            Log.e(FlahslightApplication.TAG, "flash on");
            _isFlashOn = true;
        }
    }

    private void turnOff(){
        if(_isFlashOn) {
            if(_cam == null || _params == null){
                return;
            }

            _params = _cam.getParameters();
            _params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            _cam.setParameters(_params);
            _cam.stopPreview();
//            _cam.release();

            Log.e(FlahslightApplication.TAG, "flash off");
            _isFlashOn = false;

            //TODO toggle
//            toggleButtonImage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(_hasFlash && _isFlashOn) {
            turnOn();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        turnOff();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(_cam != null){
            _cam.release();
            _cam = null;
        }
    }


}
