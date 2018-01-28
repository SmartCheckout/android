package com.smartcheckout.poc.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.smartcheckout.poc.R;

import java.io.IOException;

import static com.smartcheckout.poc.constants.constants.RC_SCAN_BARCODE_ITEM;
import static com.smartcheckout.poc.constants.constants.RC_SCAN_BARCODE_STORE;
import static com.smartcheckout.poc.constants.constants.TIMEOUT_SCAN_MILLISECS;

public class ScanBarcodeActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    private SurfaceView cameraView = null;
    private BarcodeDetector barcodeDetector = null;
    private CameraSource cameraSource = null;
    private final static int RC_CAM_PERMISSION = 1;
    private static String TAG = "ScanBarcodeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_main);

        CountDownTimer countDownTimer = new CountDownTimer(TIMEOUT_SCAN_MILLISECS, 1000) {

            public void onTick(long millisUntilFinished) {
                //TODO: Do something every second
            }

            public void onFinish() {

                Intent initiatingIntent = getIntent();
                if(initiatingIntent != null && initiatingIntent.getExtras() != null)
                {
                    int requestCode = initiatingIntent.getExtras().getInt("requestCode");
                    Intent intent = null;
                    if(requestCode == RC_SCAN_BARCODE_ITEM)
                    {
                        intent = new Intent(ScanBarcodeActivity.this, CartActivity.class);

                    }
                    else if(requestCode == RC_SCAN_BARCODE_STORE)
                    {
                        intent = new Intent(ScanBarcodeActivity.this, StoreSelectionActivity.class);

                    }
                    intent.putExtra("Reason","Timeout");
                    ScanBarcodeActivity.this.setResult(RESULT_CANCELED,intent);
                    finish();
                }


            }
        }.start();

        this.cameraView = (SurfaceView) findViewById(R.id.camera_view);
        this.barcodeDetector = new BarcodeDetector.Builder(this).build();
        this.cameraSource = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true).build();
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScanBarcodeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        String[] requiredPermission = {Manifest.permission.CAMERA};
                        ActivityCompat.requestPermissions(ScanBarcodeActivity.this, requiredPermission, RC_CAM_PERMISSION);
                    }else{
                        cameraSource.start(cameraView.getHolder());
                    }
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    Barcode barcode= barcodes.valueAt(0);
                    goToMainactivity(barcode);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RC_CAM_PERMISSION:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    try{
                        cameraSource.start(cameraView.getHolder());
                    }catch(SecurityException se){
                        Log.e(TAG,se.getMessage());
                    }catch(IOException io){
                        Log.e(TAG,io.getMessage());
                    }
                }
                else  {
                    Log.d("ScanBarcodeActivity","Permission denied");
                }
        }
    }

    public void goToMainactivity(Barcode barcode)
    {
        Intent intent = new Intent(this, CartActivity.class);
        intent.putExtra("Barcode",barcode);
        this.setResult(RESULT_OK,intent);
        finish();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {

        Intent initiatingIntent = getIntent();

        if(initiatingIntent != null && initiatingIntent.getExtras() != null)
        {
            int requestCode = initiatingIntent.getExtras().getInt("requestCode");
            Intent intent = null;
            if(requestCode == RC_SCAN_BARCODE_ITEM)
            {
                intent = new Intent(ScanBarcodeActivity.this, CartActivity.class);

            }
            else if(requestCode == RC_SCAN_BARCODE_STORE)
            {
                intent = new Intent(ScanBarcodeActivity.this, StoreSelectionActivity.class);

            }
            intent.putExtra("Reason","NA");
            ScanBarcodeActivity.this.setResult(RESULT_CANCELED,intent);
            finish();
        }
    }
}
