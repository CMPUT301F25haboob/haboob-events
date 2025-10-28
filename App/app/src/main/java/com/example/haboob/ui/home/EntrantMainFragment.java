package com.example.haboob.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.haboob.R;


// Author: David T, created on Sunday, oct 26 2025
// this fragment represents the main fragment that the entrant will see when entering the app

public class EntrantMainFragment extends Fragment {
    public EntrantMainFragment() {
        // Required empty public constructor
    }

    // “When this Fragment becomes visible, create its UI from entrant_main.xml and attach it to the container.”
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.entrant_main, container, false);
    }
}