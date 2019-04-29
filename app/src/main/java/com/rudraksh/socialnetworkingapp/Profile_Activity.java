package com.rudraksh.socialnetworkingapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Profile_Activity extends AppCompatActivity  {

    private CircleImageView profileDp;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private EditText username,fullname,dob,phno,desc;
    private AutoCompleteTextView gender;
    private Button uploadBn;
    private ProgressDialog progressDialog,newProgress;

    private volatile Uri profileDpUri;
    private volatile String downloadUri,compressedDownloadUri;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_);

        initializer();

        retrieve();

        //GRANTING PERMISSIONS
        profileDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                    } else {

                        startCropActivity();
                    }
                } else {

                    //IF BUILD VERSION IS LOWER THAN MARSHMALLOW
                    startCropActivity();

                }
            }
        });

        uploadBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (profileDpUri != null) {

                    progressDialog.setMessage("Uploading your profile ...\n*Larger files require more time to upload*");
                    progressDialog.show();

                    File imageBitmap=new File(profileDpUri.getPath());

                    try {
                        bitmap=new Compressor(getApplicationContext())
                                .setMaxHeight(125)
                                .setMaxWidth(150)
                                .setQuality(0)
                                .compressToBitmap(imageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    StorageReference compressStorage=storageReference.child("Users").child(firebaseAuth.getCurrentUser().getEmail()).child("Profile Pictures").child("compressed"+profileDpUri.getLastPathSegment());

                    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                    byte[] compressedBytes=byteArrayOutputStream.toByteArray();
                    final String compressBitmap=Base64.encodeToString(compressedBytes,Base64.DEFAULT);

                    UploadTask uploadTask=compressStorage.putBytes(compressedBytes);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()){

                                StorageReference childStorage = storageReference.child("Users").child(firebaseAuth.getCurrentUser().getEmail()).child("Profile Pictures").child(profileDpUri.getLastPathSegment());
                                childStorage.putFile(profileDpUri)
                                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                if (task.isSuccessful()){

                                                    String downloadUrimethod=task.getResult().getDownloadUrl().toString();
                                                    update(downloadUrimethod,compressBitmap);

                                                }else {
                                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                                progressDialog.dismiss();
                                            }
                                        });

                            }else {

                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                   // Log.i("CHECKHERE",downloadUri);
                    if (downloadUri==null) {
                        Toast.makeText(getApplicationContext(), "Please upload an image", Toast.LENGTH_SHORT).show();
                    }else {
                        update(downloadUri,compressedDownloadUri);

                    }
                }
            }
        });
    }

    private void update(String downloadUriCopy,String compressDownloadUri){

        if (username.getText() != null && fullname.getText() != null) {

            newProgress.setMessage("Uploading user details....");
            newProgress.show();

            final Map<String, String> info = new HashMap<>();

            info.put("Username", username.getText().toString());
            info.put("Profile Pic",downloadUriCopy);
            info.put("Full Name", fullname.getText().toString());
            info.put("Gender", gender.getText().toString());
            info.put("DOB", dob.getText().toString());
            info.put("Phone number", phno.getText().toString());
            info.put("Description", desc.getText().toString());
            info.put("Compressed Pic",compressDownloadUri);

            firestore.collection("Users").document(firebaseAuth.getCurrentUser().getEmail()).collection("User Details").document("Info").set(info)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                Toast.makeText(getApplicationContext(), "Profile uploaded successfully...", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getApplicationContext(), User_Activity.class);
                                intent.putExtra("Profile URL", profileDpUri);
                                finish();

                            } else {

                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            newProgress.dismiss();
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Please do not leave Username or Fullname empty", Toast.LENGTH_SHORT).show();
        }

    }

    private void initializer(){

        username=findViewById(R.id.usernameField);
        fullname=findViewById(R.id.fullnameField);
        dob=findViewById(R.id.dobField);
        phno=findViewById(R.id.phNoField);
        desc=findViewById(R.id.descField);

        gender=findViewById(R.id.genderField);

        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,new String[]{"Male","Female","Other"});
        gender.setAdapter(adapter);
        gender.setThreshold(1);

        profileDp=findViewById(R.id.profilePicture);

        uploadBn=findViewById(R.id.uploadBn);

        progressDialog=new ProgressDialog(this);
        newProgress=new ProgressDialog(this);

        storageReference=FirebaseStorage.getInstance().getReference();

        firebaseAuth=FirebaseAuth.getInstance();

        firestore=FirebaseFirestore.getInstance();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==1 && grantResults.length!=0 && permissions.length!=0){

           startCropActivity();
        }else
        {
            Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                profileDpUri = result.getUri();
                profileDp.setImageURI(profileDpUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.i("ERROR",error.toString());
            }
        }
        
    }

    private void startCropActivity(){

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }

    private void retrieve(){

        progressDialog.setMessage("Loading up the details....");
        progressDialog.show();
        firestore.collection("Users").document(firebaseAuth.getCurrentUser().getEmail()).collection("User Details").document("Info").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){

                            if (task.getResult().exists()){

                                username.setText(task.getResult().getString("Username"));
                                fullname.setText(task.getResult().getString("Full Name"));
                                gender.setText(task.getResult().getString("Gender"));
                                dob.setText(task.getResult().getString("DOB"));
                                phno.setText(task.getResult().getString("Phone number"));
                                desc.setText(task.getResult().getString("Description"));
                                downloadUri=task.getResult().getString("Profile Pic");
                                Glide.with(getApplicationContext()).load(downloadUri).into(profileDp);

                            }
                        }else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }
}

