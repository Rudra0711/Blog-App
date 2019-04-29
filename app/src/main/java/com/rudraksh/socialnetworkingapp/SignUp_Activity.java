package com.rudraksh.socialnetworkingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUp_Activity extends AppCompatActivity {

    public static FirebaseAuth firebaseAuth;

    private EditText email,pswrd,confirm;
    private Button signupBn,jumpToLn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_);

        firebaseAuth=FirebaseAuth.getInstance();

        email=findViewById(R.id.emailFieldSn);
        pswrd=findViewById(R.id.passwordFieldSn);
        confirm=findViewById(R.id.confirmPasswordFieldSn);

        signupBn=findViewById(R.id.signupBn);
        signupBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        jumpToLn=findViewById(R.id.jumpToLnSn);
        jumpToLn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        progressDialog=new ProgressDialog(this);
    }

    private void signup(){

        final String EMAIL=email.getText().toString();
        final String PASSWORD=pswrd.getText().toString();
        String CONFIRM=confirm.getText().toString();

            if (PASSWORD.equals(CONFIRM)) {

                progressDialog.setMessage("Creating your account...");
                progressDialog.show();

                try {
                    firebaseAuth.createUserWithEmailAndPassword(EMAIL, PASSWORD)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Account created successfully", Toast.LENGTH_SHORT).show();

                                        firebaseAuth.signInWithEmailAndPassword(EMAIL,PASSWORD)
                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()){

                                                    Toast.makeText(getApplicationContext(), "Successfully logged in", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(getApplicationContext(),Profile_Activity.class));
                                                    finish();

                                                }else {
                                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                    finish();
                                                }
                                            }
                                        });

                                    } else {

                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });
                } catch (IllegalArgumentException ex) {

                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Do not leave the field(s) empty", Toast.LENGTH_SHORT).show();
                }
            } else {

                Toast.makeText(getApplicationContext(), "Try Again!!\nPasswords not matched.. ", Toast.LENGTH_SHORT).show();

            }

    }


}
