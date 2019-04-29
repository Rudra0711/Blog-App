package com.rudraksh.socialnetworkingapp;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.auth.User;


/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends Fragment {


    public BlankFragment() {
        // Required empty public constructor
    }


    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_blank, container, false);

        User_Activity.floatingActionButton.setVisibility(View.GONE);
        User_Activity.frameLayout.setVisibility(View.GONE);
        User_Activity.bottomNavigationView.setVisibility(View.GONE);
        User_Activity.toolbar.setVisibility(View.GONE);

        return view;
    }

}
