package com.rudraksh.socialnetworkingapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter adapter;
    private List<Post_Instance> list=new ArrayList<>();

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private ProgressDialog progressDialog;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView=view.findViewById(R.id.recyclerView);
        layoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter=new RecyclerAdapter(list,getContext());
        recyclerView.setAdapter(adapter);

        firebaseAuth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();

        progressDialog=new ProgressDialog(getContext());

        getPost();

        User_Activity.postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                manipulateList();



            }
        });

        User_Activity.closeComment.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {

                User_Activity.slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                User_Activity.floatingActionButton.setVisibility(View.VISIBLE);
                User_Activity.bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });


        return view;
    }

    public void manipulateList() {

        progressDialog.setMessage("Posting your comment...");
        progressDialog.show();


        firestore.collection("Users").document(firebaseAuth.getCurrentUser().getEmail()).collection("User Details").document("Info").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){

                            if (task.getResult().exists()){

                                final String username=task.getResult().getString("Username");
                                final String downloadUri=task.getResult().getString("Profile Pic");
                                final String date=DateFormat.getDateInstance(DateFormat.DEFAULT,new Locale("en","In")).format(new Date());
                                final String comment=User_Activity.commentEditText.getText().toString();

                                firestore.collection("Current Post").document("getCurrentPost").get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()){

                                            String currentPost=task.getResult().getString("Current post");

                                            Map<String,String> commentInfo=new HashMap<>();
                                            commentInfo.put("Username",username);
                                            commentInfo.put("DownloadProfileUri",downloadUri);
                                            commentInfo.put("Date",date);
                                            commentInfo.put("The Comment",comment);

                                            String time=DateFormat.getTimeInstance(DateFormat.DEFAULT,new Locale("en","In")).format(new Date());

                                            firestore.collection("Posts").document(currentPost).collection("Comments").document(time)
                                                    .set(commentInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){

                                                        User_Activity.list.add(new ListInstance(downloadUri,username,date,comment));
                                                        adapter.notifyDataSetChanged();
                                                        User_Activity.commentEditText.setText("");
                                                        Toast.makeText(getContext(),"Comment Posted",Toast.LENGTH_SHORT).show();
                                                    }else {

                                                        Toast.makeText(getContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }else {

                                            Toast.makeText(getContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                        progressDialog.dismiss();
                                    }
                                });


                            }
                        }else {
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }


    private void getPost(){


        firestore.collection("Posts").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){

                            if (!task.getResult().isEmpty()){

                                int i=0;
                                list.clear();
                                Iterator<QueryDocumentSnapshot> iterator=task.getResult().iterator();
                                while (iterator.hasNext()){

                                    Map<String, Object> postInfo= task.getResult().getDocuments().get(i).getData();
                                    i++;

                                    String DPUri= String.valueOf(postInfo.get("Profile DP"));
                                    String postUri= String.valueOf(postInfo.get("Image"));
                                    String username= String.valueOf(postInfo.get("Username"));
                                    String desc= String.valueOf(postInfo.get("Description"));
                                    String likeCount= "0";
                                    String commentCount= "0";
                                    String time= String.valueOf(postInfo.get("Time"));

                                    list.add(new Post_Instance(DPUri,postUri,username,desc,likeCount,commentCount,time));

                                    iterator.next();
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }else {

                        }
                    }
                });

    }

}
