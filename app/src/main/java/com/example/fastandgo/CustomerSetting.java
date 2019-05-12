package com.example.fastandgo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CustomerSetting extends AppCompatActivity {
    private EditText nameField, numberField;
    private Button confirmBtn, backBtn;
    private FirebaseAuth auth;
    private DatabaseReference customerDatabase;
    private String userId, nameStr, phoneStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_setting);
        nameField=(EditText) findViewById(R.id.name);
        numberField=(EditText) findViewById(R.id.phone);

        confirmBtn=(Button) findViewById(R.id.confirmBtn);
        backBtn=(Button) findViewById(R.id.backBtn);


        auth=FirebaseAuth.getInstance();
        userId=auth.getCurrentUser().getUid();
        customerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(userId);
        getUserInfo();
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfromation();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });




    }
    private void getUserInfo(){
        customerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&& dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        nameStr=map.get("name").toString();
                        nameField.setText(nameStr);

                    }
                    if(map.get("phone")!=null){
                        phoneStr=map.get("phone").toString();
                        numberField.setText(phoneStr);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void saveUserInfromation(){
        nameStr=nameField.getText().toString();
        phoneStr=numberField.getText().toString();

        Map userinfo=new HashMap();
        userinfo.put("name",nameStr);
        userinfo.put("phone",phoneStr);
        customerDatabase.updateChildren(userinfo);
        finish();

    }
}
