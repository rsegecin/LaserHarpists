package com.rmscore.laserharpists;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rmscore.bases.BaseActivity;
import com.rmscore.utils.SimpleArrayAdapter;

import java.util.ArrayList;

public class LearnToPlayList extends BaseActivity {

    public static final String MUSIC_CHOSEN = "MUSIC_CHOSEN";
    private ArrayList<String> MusicList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_to_play_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MusicList.add("Tic Tac Toe");
        MusicList.add("Cai Cai Balão");
        MusicList.add("Brilha Brilha Estrelinha");

        SimpleArrayAdapter msgAdapter =
            new SimpleArrayAdapter(this, R.layout.listview_item,
                MusicList, SimpleArrayAdapter.eTextAlign.left);

        ListView lvMusics = (ListView) findViewById(R.id.lvMusics);
        lvMusics.setAdapter(msgAdapter);
        lvMusics.setOnItemClickListener(new OnMessageClicked());
    }

    @Override
    public void ServiceStarted() {
        super.ServiceStarted();
    }

    public class OnMessageClicked implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(LearnToPlayList.this, ScoreGame.class);
            intent.putExtra(MUSIC_CHOSEN, position);
            startActivity(intent);
        }
    }
}
