package com.rudraksh.socialnetworkingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private List<Post_Instance> list;
    private Context context;

    private volatile String postImageUri;
    private volatile String modifiedpostImageUri;
    private boolean[] status;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public RecyclerAdapter(List<Post_Instance> list, Context context) {
        this.list = list;
        this.context = context;

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View post=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_layout,viewGroup,false);

        return new MyViewHolder(post);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, final int i) {

        status = new boolean[list.size()];
        postImageUri=list.get(i).getPostPic();
        Glide.with(context).load(list.get(i).getProfileDp()).into(viewHolder.profileDp);
        Glide.with(context).load(postImageUri).into(viewHolder.postPic);
        viewHolder.commentIc.setImageResource(R.drawable.ic_comment);

        viewHolder.username.setText(list.get(i).getUsername());
        viewHolder.time.setText(list.get(i).getTime()+"");
        viewHolder.desc.setText(list.get(i).getDesc());

        modifiedpostImageUri=postImageUri.replaceAll("/", "+");

        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getEmail()).collection("Liked Posts").document(modifiedpostImageUri).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            if (task.getResult().exists()){

                                boolean checker= (boolean) task.getResult().get("Status");

                                if (checker){
                                    viewHolder.likeIc.setImageResource(R.drawable.ic_favorite_liked);
                                    status[i] =checker;
                                }else{
                                    viewHolder.likeIc.setImageResource(R.drawable.ic_like);
                                    status[i] =checker;
                                }
                            }

                        }else {

                            Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        firebaseFirestore.collection("Likes").document(modifiedpostImageUri).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            if (task.getResult().exists()){


                                long Count= (long) task.getResult().get("Likes");
                                viewHolder.likeCount.setText(String.valueOf(Count));
                            }

                        }else {

                            Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        firebaseFirestore.collection("Comments").document(modifiedpostImageUri).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            if (task.getResult().exists()){

                                long Count= (long) task.getResult().get("Comments");
                                viewHolder.commentCount.setText(String.valueOf(Count));
                            }

                        }else {

                            Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileDp;
        private ImageView postPic,likeIc,commentIc;
        private TextView username,time,desc,likeCount,commentCount;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);

            profileDp=itemView.findViewById(R.id.postProfile);
            postPic=itemView.findViewById(R.id.postImage);
            likeIc=itemView.findViewById(R.id.postLike);
            commentIc=itemView.findViewById(R.id.postComment);
            username=itemView.findViewById(R.id.postUsername);
            time=itemView.findViewById(R.id.postTime);
            desc=itemView.findViewById(R.id.postDesc);
            likeCount=itemView.findViewById(R.id.postLikeCount);
            commentCount=itemView.findViewById(R.id.postCommentCount);


            likeIc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {



                    if (status[getAdapterPosition()]) {

                        likeIc.setImageResource(R.drawable.ic_like);
                        status[getAdapterPosition()] =false;

                        Map<String,Boolean> statusMap=new HashMap<>();
                        statusMap.put("Status",status[getAdapterPosition()]);

                        modifiedpostImageUri=list.get(getAdapterPosition()).getPostPic().replaceAll("/", "+");

                        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getEmail()).collection("Liked Posts").document(modifiedpostImageUri).set(statusMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()){

                                            Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        firebaseFirestore.collection("Likes").document(modifiedpostImageUri).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            if (task.getResult().exists()){

                                                long count= (long) task.getResult().get("Likes");
                                                count-=1;
                                                updateLikes(count);
                                            }

                                        }else {

                                            Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }else {

                        likeIc.setImageResource(R.drawable.ic_favorite_liked);
                        status[getAdapterPosition()] =true;

                        Map<String,Boolean> statusMap=new HashMap<>();
                        statusMap.put("Status",status[getAdapterPosition()]);

                        modifiedpostImageUri=list.get(getAdapterPosition()).getPostPic().replaceAll("/", "+");

                        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getEmail()).collection("Liked Posts").document(modifiedpostImageUri).set(statusMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()){

                                            Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        firebaseFirestore.collection("Likes").document(modifiedpostImageUri).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            if (task.getResult().exists()){

                                                  long count=(long) task.getResult().get("Likes");
                                                count+=1;
                                                updateLikes(count);
                                            }

                                        }else {

                                            Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            });


            commentIc.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onClick(View v) {

                    String modifiedPath=list.get(getAdapterPosition()).getPostPic().replace("/","+");
                    Log.i("modifiedPath", String.valueOf(getAdapterPosition()));

                    Map<String,String> curr=new HashMap<>();
                    curr.put("Current post",modifiedPath);

                    firebaseFirestore.collection("Current Post").document("getCurrentPost").set(curr)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                    }else {
                                        Toast.makeText(context,task.getException().getMessage()+"\nTry Again",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                    firebaseFirestore.collection("Posts").document(modifiedPath).collection("Comments")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()){

                                User_Activity.list.clear();
                                User_Activity.adapter.notifyDataSetChanged();

                                if (!task.getResult().isEmpty()){

                                    int i=0;

                                    Iterator<QueryDocumentSnapshot> iterator=task.getResult().iterator();

                                    while (iterator.hasNext()){

                                        Map<String, Object> comments=task.getResult().getDocuments().get(i).getData();
                                        i++;

                                        String username= String.valueOf(comments.get("Username"));
                                        String profileUri= String.valueOf(comments.get("DownloadProfileUri"));
                                        String date= String.valueOf(comments.get("Date"));
                                        String comment= String.valueOf(comments.get("The Comment"));

                                        User_Activity.list.add(new ListInstance(profileUri,username,date,comment));
                                        //Log.i("LIST", String.valueOf(list.get(i-2)));

                                        iterator.next();
                                    }

                                    Map<String,Long> counterMapC=new HashMap<>();
                                    counterMapC.put("Comments", (long) i-1);


                                    firebaseFirestore.collection("Comments").document(modifiedpostImageUri).set(counterMapC)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (!task.isSuccessful()){

                                                        Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                    User_Activity.adapter.notifyDataSetChanged();
                                }
                                else{

                                    User_Activity.list.add(new ListInstance("","","","Be the first one to post a comment on this post"));
                                    User_Activity.adapter.notifyDataSetChanged();
                                }

                            }else {

                            }
                        }
                    });


                    User_Activity.slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    User_Activity.slidingPaneLayout.setPanelHeight(170);

                    User_Activity.floatingActionButton.setVisibility(View.GONE);
                    User_Activity.bottomNavigationView.setVisibility(View.GONE);

                }
            });
        }

        private void updateLikes(final long count) {

            Map<String,Long> counterMap=new HashMap<>();
            counterMap.put("Likes",count);

            modifiedpostImageUri=list.get(getAdapterPosition()).getPostPic().replaceAll("/", "+");

            firebaseFirestore.collection("Likes").document(modifiedpostImageUri).set(counterMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                likeCount.setText(String.valueOf(count));
                            }else {
                                Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
