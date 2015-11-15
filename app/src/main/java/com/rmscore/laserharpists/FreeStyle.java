package com.rmscore.laserharpists;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.rmscore.bases.BaseActivity;
import com.rmscore.utils.SimpleArrayAdapter;

import java.util.ArrayList;

public class FreeStyle extends BaseActivity {

    private ArrayList<String> Instrument = new ArrayList<>();
    private ArrayList<String> Records = new ArrayList<>();

    private Spinner spinnerSoundType;
    private Spinner spinnerRecords;
    private ImageView imgFretOne;
    private ImageView imgFretTwo;
    private ImageView imgFretThree;
    private ImageView imgFretFour;
    private ImageView imgFretFive;
    private ImageView imgFretSix;
    private ImageView imgFretSeven;
    private ImageView imgFretEight;
    private Button btnPlay;
    private Button btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_style);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InitLayout();
    }

    private void InitLayout() {

        Instrument.add("Piano");
        Instrument.add("Flauta");
        Instrument.add("Saxofone");

        SimpleArrayAdapter adapterIntruments =
                new SimpleArrayAdapter(this, R.layout.listview_item,
                        Instrument, SimpleArrayAdapter.eTextAlign.left);

        spinnerSoundType = (Spinner) findViewById(R.id.spinnerSoundType);
        spinnerSoundType.setAdapter(adapterIntruments);
        spinnerSoundType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Records.add("Records");

        SimpleArrayAdapter adapterRecords =
                new SimpleArrayAdapter(this, R.layout.listview_item,
                        Records, SimpleArrayAdapter.eTextAlign.left);

        spinnerRecords = (Spinner) findViewById(R.id.spinnerRecords);
        spinnerRecords.setAdapter(adapterRecords);
        spinnerRecords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerRecords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        imgFretOne = (ImageView) findViewById(R.id.imgFretOne);
        imgFretTwo = (ImageView) findViewById(R.id.imgFretTwo);
        imgFretThree = (ImageView) findViewById(R.id.imgFretThree);
        imgFretFour = (ImageView) findViewById(R.id.imgFretFour);
        imgFretFive = (ImageView) findViewById(R.id.imgFretFive);
        imgFretSix = (ImageView) findViewById(R.id.imgFretSix);
        imgFretSeven = (ImageView) findViewById(R.id.imgFretSeven);
        imgFretEight = (ImageView) findViewById(R.id.imgFretEight);

        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RmsService.SendToBluetooth("Yellowwww");
            }
        });

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RmsService.SendToBluetooth("Awww Morty what are you doing?");
            }
        });

    }

    @Override
    public void ServiceStarted() {
        super.ServiceStarted();
    }
}
