package com.smartcheckout.poc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.smartcheckout.poc.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button signOutButton = (Button) findViewById(R.id.sign_out);
        signOutButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("On click of sign out");
                        if (v.getId() == R.id.sign_out) {
                            AuthUI.getInstance()
                                    .signOut(SettingsActivity.this)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        public void onComplete(@NonNull Task<Void> task) {
                                            startActivity(new Intent(SettingsActivity.this, PostSignOut.class));
                                            finish();
                                        }
                                    });
                        }
                    }
                });
    }
}
