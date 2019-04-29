package com.rudraksh.socialnetworkingapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListAdapter extends ArrayAdapter<ListInstance> {

    private List<ListInstance> list;
    private Context context;

    public ListAdapter(Context context, List<ListInstance> list) {
        super(context, R.layout.commentlayout,list);
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View row=LayoutInflater.from(parent.getContext()).inflate(R.layout.commentlayout,parent,false);

        CircleImageView profileComment=row.findViewById(R.id.profileComment);
        TextView username=row.findViewById(R.id.commentUsername);
        TextView date=row.findViewById(R.id.commentDate);
        TextView comment=row.findViewById(R.id.TheComment);

        Glide.with(getContext()).load(list.get(position).getProfileUri()).into(profileComment);
        username.setText(list.get(position).getUsername());
        date.setText(list.get(position).getdateOfPosting());
        comment.setText(list.get(position).getComment());

        return row;
    }
}
