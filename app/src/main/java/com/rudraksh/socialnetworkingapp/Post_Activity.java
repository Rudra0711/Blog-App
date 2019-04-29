package com.rudraksh.socialnetworkingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Post_Activity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView imageView;
    private EditText descField;

    private Uri imageUri;

    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_);

        toolbar=findViewById(R.id.post_toolbar);
        setSupportActionBar(toolbar);

        descField=findViewById(R.id.descField);
        imageView=findViewById(R.id.postImageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(Post_Activity.this);
            }
        });

        firebaseAuth=FirebaseAuth.getInstance();
        storageReference=FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();

        progressDialog=new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult resultData = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri=resultData.getUri();
                imageView.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Toast.makeText(getApplicationContext(),resultData.getError().getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.post_toolbar_layout,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getEmail()).collection("User Details").document("Info").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){

                            if (task.getResult().exists()){

                                String usernameMain = task.getResult().getString("Username");
                                String downloadUri = task.getResult().getString("Profile Pic");
                                uploadPost(usernameMain,downloadUri);
                            }
                        }else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return true;
    }

    private void uploadPost(final String username,final String downloadUriPP){

        final String desc=descField.getText().toString();

        if (imageUri!=null && desc!=""){

            progressDialog.setMessage("Uploading your post....");
            progressDialog.show();

            StorageReference postPath=storageReference.child("Users").child(firebaseAuth.getCurrentUser().getEmail()).child("Posts").child(imageUri.getLastPathSegment());
            postPath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        String date=DateFormat.getDateInstance(DateFormat.DEFAULT,new Locale("en","IN")).format(new Date());
                        String time=DateFormat.getTimeInstance(DateFormat.DEFAULT,new Locale("en","IN")).format(new Date());
                        final String postImageUri= task.getResult().getDownloadUrl().toString();

                        final Map<String,Object> postInfo=new HashMap<>();
                        postInfo.put("Description", desc);
                        postInfo.put("Image",postImageUri);
                        postInfo.put("Time",date+" at "+time);
                        postInfo.put("Username", username);
                        postInfo.put("Profile DP",downloadUriPP);
                        Log.i("Username",username);

                        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getEmail()).collection("Posts").document(imageUri.getLastPathSegment()).set(postInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){

                                            String modifiedpostImageUri=postImageUri.replaceAll("/", "+");

                                            firebaseFirestore.collection("Posts").document(modifiedpostImageUri).set(postInfo)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()){

                                                                Toast.makeText(getApplicationContext(),"Post uploaded successfully....",Toast.LENGTH_SHORT).show();
                                                                startActivity(new Intent(getApplicationContext(),User_Activity.class));
                                                                finish();

                                                            }else {

                                                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                            Map<String,Long> counterMap=new HashMap<>();
                                            counterMap.put("Likes",0l);
                                            counterMap.put("Comments",0l);


                                            firebaseFirestore.collection("Likes").document(modifiedpostImageUri).set(counterMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (!task.isSuccessful()){

                                                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                            Map<String,Long> counterMapC=new HashMap<>();
                                            counterMapC.put("Comments",0l);


                                            firebaseFirestore.collection("Comments").document(modifiedpostImageUri).set(counterMapC)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (!task.isSuccessful()){

                                                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });


                                            Map<String,Boolean> statusMap=new HashMap<>();
                                            statusMap.put("Status",false);

                                            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getEmail()).collection("Liked Posts").document(modifiedpostImageUri).set(statusMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (!task.isSuccessful()){

                                                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                        }else {

                                            Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });



                    }else{
                        Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }else {

            Toast.makeText(getApplicationContext(),"Image or Description cannot be left empty",Toast.LENGTH_LONG).show();
        }


    }

}
