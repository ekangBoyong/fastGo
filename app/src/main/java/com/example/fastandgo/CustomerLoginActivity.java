package com.example.fastandgo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginActivity extends AppCompatActivity implements View.OnClickListener{
        private Button btnLogin;
        private EditText txtEmail;
        private EditText txtPassword;
        private TextView txtViewSignin;
        private ProgressBar mProgressBar;
        private TextView mLoadingText;
        private int mProgressStatus = 0;
        private Handler mHandler = new Handler();
        private FirebaseAuth firebaseAuth;
        private FirebaseAuth.AuthStateListener firebaseListener;
        private  String usern;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnLogin =(Button) findViewById(R.id.loginButton);
        txtEmail=(EditText) findViewById(R.id.emailAddress);
        txtPassword=(EditText) findViewById(R.id.password);
        txtViewSignin=(TextView) findViewById(R.id.ViewSign);
        btnLogin.setOnClickListener(this);
        txtViewSignin.setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mLoadingText = (TextView) findViewById(R.id.LoadingCompleteTextView);
        mProgressBar.setVisibility(View.INVISIBLE);
        firebaseAuth= FirebaseAuth.getInstance();




    }
    private void LoginUser(){
        String emailAdd=txtEmail.getText().toString().trim();
        String password=txtPassword.getText().toString().trim();
        firebaseAuth.signInWithEmailAndPassword(emailAdd,password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String user_id=firebaseAuth.getCurrentUser().getUid();
                    DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(user_id);
                    current_user_db.setValue(true);
                    Toast.makeText(CustomerLoginActivity.this,"Success",Toast.LENGTH_SHORT).show();
                    LoginSuccess();

                }else{
                    Toast.makeText(CustomerLoginActivity.this,"Email or Password is Incorrect",Toast.LENGTH_SHORT).show();

                }
            }
        });
        /**firebaseListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    LoginSuccess();
                    
                }

            }
        };**/

    }
    private void registerUser(){
        String emailAdd=txtEmail.getText().toString().trim();
        String password=txtPassword.getText().toString().trim();
        if(TextUtils.isEmpty(emailAdd)){
            //email is empty
            Toast.makeText(this,"Please Enter Your Emaill Address",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            //password is empty
            Toast.makeText(this,"Please Enter Your Password",Toast.LENGTH_SHORT).show();
            return;


        }
        Progressbar();
        firebaseAuth.createUserWithEmailAndPassword(emailAdd,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    Toast.makeText(CustomerLoginActivity.this,"Success",Toast.LENGTH_SHORT).show();
                    String user_id=firebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Admin").child(usern);
                    current_user_db.setValue(true);

                }else{
                    Toast.makeText(CustomerLoginActivity.this,"Failed",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
   public void LoginSuccess(){

            Intent i = new Intent(this, CustomerMapActivity.class);

            startActivity(i);
            finish();
            return;


   }
    private void Progressbar(){
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mProgressStatus < 100){
                    mProgressStatus++;
                    android.os.SystemClock.sleep(50);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(mProgressStatus);
                        }
                    });
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingText.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }).start();
    }
    @Override
    public void onClick(View v) {
        if(v== btnLogin){
            //registerUser();
            LoginUser();
        }if(v==txtViewSignin){

        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(firebaseListener);
    }
}
