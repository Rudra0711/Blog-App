package com.rudraksh.socialnetworkingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.renderscript.RenderScript;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    public static FirebaseAuth firebaseAuth;

    private EditText emailField,passwordField;
    private Button loginBn,jumpToSn;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth=FirebaseAuth.getInstance();

        emailField=findViewById(R.id.emailField);
        passwordField=findViewById(R.id.passwordField);

        loginBn=findViewById(R.id.loginBn);

        loginBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        progressDialog=new ProgressDialog(this);

        jumpToSn=findViewById(R.id.jumpToSn);
        jumpToSn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignUp_Activity.class));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user=firebaseAuth.getCurrentUser();

        if (user!=null ){

            startActivity(new Intent(getApplicationContext(),User_Activity.class));
            finish();
        }
    }

    private void login(){

       String EMAIL=emailField.getText().toString();
       String PASSWORD=passwordField.getText().toString();

       progressDialog.setMessage("Verifying your details...");
       progressDialog.show();

      try {
           firebaseAuth.signInWithEmailAndPassword(EMAIL, PASSWORD)
                   .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {

                           if (task.isSuccessful()) {

                               progressDialog.dismiss();
                               Toast.makeText(getApplicationContext(), "Successfully signed in", Toast.LENGTH_SHORT).show();
                               startActivity(new Intent(getApplicationContext(),User_Activity.class));
                               finish();

                           } else {

                               progressDialog.dismiss();
                               passwordField.setText("");
                               Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                           }

                       }
                   });
       }catch (IllegalArgumentException ex){

          progressDialog.dismiss();
          Toast.makeText(getApplicationContext(), "Do not leave the field(s) empty", Toast.LENGTH_SHORT).show();
      }
    }
}
