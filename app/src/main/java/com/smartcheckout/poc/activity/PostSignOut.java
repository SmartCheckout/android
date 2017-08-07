package com.smartcheckout.poc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.smartcheckout.poc.R;

public class PostSignOut extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_sign_out);
        Button postSignOutSignIn = (Button) findViewById(R.id.post_sign_out_sign_in);
        postSignOutSignIn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("In on click listener for postSignOutSignIn and starting login activity");
                        startActivity(new Intent(PostSignOut.this, LoginActivity.class));
                    }
                });
    }
}
