package com.rmscore.laserharpists;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.rmscore.bases.BaseActivity;

public class Welcome extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellcome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void onBackPressed() {
        UnbindService();
    }

    @Override
    public void ServiceStarted() {
        super.ServiceStarted();

        Button btnLearnToPlay = (Button) findViewById(R.id.btnLearnToPlay);
        btnLearnToPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rmsService.IsBluetoothConnected()) {
                    Intent intent = new Intent(Welcome.this, ScoreGame.class);
                    startActivity(intent);
                }
                else {
                    MakeBluetoothHappen();
                }
            }
        });

        Button btnFreeStyle = (Button) findViewById(R.id.btnFreeStyle);
        btnFreeStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rmsService.IsBluetoothConnected()) {
                    Intent intent = new Intent(Welcome.this, FreeStyle.class);
                    startActivity(intent);
                }
                else {
                    MakeBluetoothHappen();
                }
            }
        });

        ImageButton imgBtnAngle = (ImageButton) findViewById(R.id.imgBtnAngle);
        imgBtnAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MakeBluetoothHappen();
            }
        });
    }
}
