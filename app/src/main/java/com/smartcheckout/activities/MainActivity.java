package com.smartcheckout.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import activities.smartcheckout.R;

public class MainActivity extends AppCompatActivity {
    TextView mainArea;

    private static final int RC_BARCODE_CAPTURE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainArea = (TextView) findViewById(R.id.main_area);
    }

    public void scanBarCode(View view)
    {
        Intent intent = new Intent(this,ScanBarcodeActivity.class);
        startActivityForResult(intent,RC_BARCODE_CAPTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_BARCODE_CAPTURE)
        {
            if(resultCode == CommonStatusCodes.SUCCESS){
                if(data != null)
                {
                    Barcode barcode = data.getParcelableExtra("barcode");
                    mainArea.setText("Barcode value" + barcode.displayValue);
                }
                else
                {
                    mainArea.setText("Oops! We could not scan the barcode");
                }
            }

        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}