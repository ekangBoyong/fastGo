package com.example.fastandgo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FrontPageActivity extends AppCompatActivity {
    private Button adminbtn, customerbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);

        adminbtn=(Button) findViewById(R.id.AdminBtn);
        customerbtn=(Button) findViewById(R.id.CustomerBtn);


        adminbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(FrontPageActivity.this,AdminLoginActivity.class);
                startActivity(intent);

                return;
            }
        });

        customerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(FrontPageActivity.this,CustomerLoginActivity.class);
                startActivity(intent);

                return;
            }
        });

    }
}
