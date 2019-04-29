package com.rudraksh.socialnetworkingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class User_Activity extends AppCompatActivity {

    public static Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    public static FloatingActionButton floatingActionButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    public static BottomNavigationView bottomNavigationView;
    public static SlidingUpPanelLayout slidingPaneLayout;
    public static FrameLayout frameLayout;
    public static ConstraintLayout constraintLayout;

    public static ImageView postComment,closeComment;

    public static ListView listView;

    public static List<ListInstance> list;
    public static ListAdapter adapter;
    public static EditText commentEditText;

    public static LinearLayout commentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_);

        frameLayout=findViewById(R.id.fragmentContainer);

        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer,new HomeFragment()).commit();

        drawerLayout=findViewById(R.id.drawerLayout);

        toolbar=findViewById(R.id.include);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);

        navigationView=findViewById(R.id.navigationView);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.profileItem:
                        drawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(),Profile_Activity.class));
                        return true;
                    case R.id.logoutItem:
                        drawerLayout.closeDrawers();
                        MainActivity.firebaseAuth.signOut();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));

                        return true;
                }

                return false;
            }
        });

        slidingPaneLayout=findViewById(R.id.sliding_layout);

        floatingActionButton=findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                 startActivity(new Intent(getApplicationContext(),Post_Activity.class));


            }
        });

        firebaseAuth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();

       bottomNavigationView=findViewById(R.id.bottomNavigationView);

       bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.homeBottomView:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,new HomeFragment()).commit();
                        return true;
                    case R.id.notificationBottomView:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,new NotificationFragment()).commit();
                        return true;
                    case R.id.accountBottomView:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,new AccountFragment()).commit();
                        return true;
                }

                return true;
            }
        });

       listView=findViewById(R.id.listView);

        list=new ArrayList<>();

        adapter=new ListAdapter(getApplicationContext(),list);
        listView.setAdapter(adapter);

        commentEditText=findViewById(R.id.commentEditText);

        commentLayout=findViewById(R.id.commentLinearLayout);

        postComment=findViewById(R.id.postComment);

        closeComment=findViewById(R.id.closeComments);


    }



    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth=FirebaseAuth.getInstance();

        //Checking whether the user has filled his profile or not.
        firestore.collection("Users").document(firebaseAuth.getCurrentUser().getEmail()).collection("User Details").document("Info").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){

                            if (task.getResult().exists()){

                                if (task.getResult().getString("Username")==null || task.getResult().getString("Full Name")==null || task.getResult().getString("Profile Pic")==null) {
                                    if (task.getResult().getString("Username") == null) {

                                        Toast.makeText(getApplicationContext(), "Username cannot be left empty", Toast.LENGTH_SHORT).show();
                                    }
                                    if (task.getResult().getString("Full Name") == null) {

                                        Toast.makeText(getApplicationContext(), "Full Name cannot be left empty", Toast.LENGTH_SHORT).show();
                                    }
                                    if (task.getResult().getString("Profile Pic") == null) {

                                        Toast.makeText(getApplicationContext(), "Profile picture cannot be left empty", Toast.LENGTH_SHORT).show();
                                    }

                                    startActivity(new Intent(getApplicationContext(),Profile_Activity.class));
                                }
                            }else {

                                startActivity(new Intent(getApplicationContext(),Profile_Activity.class));
                                Toast.makeText(getApplicationContext(), "Profile picture, Username and Full Name cannot be left empty", Toast.LENGTH_LONG).show();
                            }

                        }else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        drawerLayout.openDrawer(Gravity.START);
        return super.onOptionsItemSelected(item);
    }
}
