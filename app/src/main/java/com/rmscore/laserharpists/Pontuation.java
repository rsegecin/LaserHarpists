package com.rmscore.laserharpists;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rmscore.bases.BaseActivity;

public class Pontuation extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pontuation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

}
