package com.rmscore.laserharpists;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rmscore.bases.BaseActivity;

public class ScoreGame extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent myIntent = getIntent();
        int music_chosen = myIntent.getIntExtra(LearnToPlayList.MUSIC_CHOSEN, 0);

        toast("You've chosen " + music_chosen);
    }

}
