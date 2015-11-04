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
                if (RmsService.IsBluetoothConnected()) {
                    Intent intent = new Intent(Welcome.this, LearnToPlayList.class);
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
                if (RmsService.IsBluetoothConnected()) {
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

    @Override
    public void Interpreter(String msg) {
        super.Interpreter(msg);

        if (!msg.equals(getString(R.string.harp_hand_shake))) {
            String not_harp = getString(R.string.not_harp);
            showAlertDialog(String.format(not_harp, RmsService.BluetoothDeviceName));
            RmsService.DisconnectBluetooth();
        }
        else {
            String harp_connected = getString(R.string.harp_connected);
            showAlertDialog(String.format(harp_connected, RmsService.BluetoothDeviceName));
        }
    }
}
