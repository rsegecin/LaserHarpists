package com.rmscore.laserharpists;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.rmscore.bases.BaseActivity;
import com.rmscore.controls.Chronometer;
import com.rmscore.datamodels.MusicData;
import com.rmscore.datamodels.NoteData;
import com.rmscore.music.INoteReceiver;
import com.rmscore.utils.SimpleArrayAdapter;

import java.util.ArrayList;

public class FreeStyle extends BaseActivity implements INoteReceiver {

    private ArrayList<MusicData> Musics = new ArrayList<>();
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
    private Button btnMusicAction;
    private Button btnRecord;
    private Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_style);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InitLayout();
    }

    @Override
    public void ServiceStarted() {
        super.ServiceStarted();
    }

    private void InitLayout() {
        SimpleArrayAdapter adapterInstruments =
                new SimpleArrayAdapter(this, R.layout.listview_item,
                        RmsService.musicManager.GetInstruments(), SimpleArrayAdapter.eTextAlign.left);

        spinnerSoundType = (Spinner) findViewById(R.id.spinnerSoundType);
        spinnerSoundType.setAdapter(adapterInstruments);
        spinnerSoundType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        UpdateSpinnerRecords();

        imgFretOne = (ImageView) findViewById(R.id.imgFretOne);
        imgFretTwo = (ImageView) findViewById(R.id.imgFretTwo);
        imgFretThree = (ImageView) findViewById(R.id.imgFretThree);
        imgFretFour = (ImageView) findViewById(R.id.imgFretFour);
        imgFretFive = (ImageView) findViewById(R.id.imgFretFive);
        imgFretSix = (ImageView) findViewById(R.id.imgFretSix);
        imgFretSeven = (ImageView) findViewById(R.id.imgFretSeven);
        imgFretEight = (ImageView) findViewById(R.id.imgFretEight);

        chronometer = (Chronometer) findViewById(R.id.chronometer);

        btnMusicAction = (Button) findViewById(R.id.btnMusicAction);
        btnMusicAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((!RmsService.musicManager.IsRecording) && (spinnerRecords.getSelectedItemPosition() > 0)) {
                    if (!RmsService.musicManager.IsPlaying) {
                        MusicData music = Musics.get(spinnerRecords.getSelectedItemPosition() - 1); // Because add "Musics Recorded" in the first line
                        RmsService.musicManager.PlayMusic(music);
                        chronometer.start();
                        btnMusicAction.setText("Stop");
                    } else {
                        RmsService.musicManager.StopMusic();
                        btnMusicAction.setText("Play");
                        chronometer.stop();
                    }
                } else {
                    showAlertDialog("Cannot play music while recording.");
                }
            }
        });

        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!RmsService.musicManager.IsRecording) {
                    RmsService.musicManager.StartRecording(spinnerSoundType.getSelectedItemPosition());
                    btnRecord.setText("Recording");
                    btnRecord.setBackgroundColor(0xFFDF3831);
                    chronometer.start();
                } else {
                    try {
                        RmsService.musicManager.StopRecording();
                    } catch (Exception e) {
                        FreeStyle.this.showAlertDialog(e.getMessage());
                    }
                    btnRecord.setText("Record");
                    btnRecord.setBackgroundColor(Color.LTGRAY);
                    chronometer.reset();
                }
            }
        });

    }

    private void UpdateSpinnerRecords() {
        ArrayList<String> strMusics;

        Musics = RmsService.musicManager.GetMusics();
        strMusics = RmsService.musicManager.GetStrMusics(Musics);

        strMusics.add(0, "Musics Recorded");

        SimpleArrayAdapter adapterRecords =
                new SimpleArrayAdapter(this, R.layout.listview_item,
                        strMusics, SimpleArrayAdapter.eTextAlign.left);

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
                MessageDialog messageDialog = new MessageDialog(FreeStyle.this.getBaseContext(), position);
                messageDialog.show();
                return false;
            }
        });
    }

    @Override
    public void onNoteReceived(NoteData noteData) {

        if (noteData.NoteDirection == NoteData.eNoteDirection.Input) {
            SetFretImg(noteData.Chord, R.drawable.redfret);
        } else {
            SetFretImg(noteData.Chord, R.drawable.blankfret);
        }

    }

    private void SetFretImg(int chord, int color) {
        switch (chord) {
            case 0:
                imgFretOne.setImageResource(color);
                break;
            case 1:
                imgFretTwo.setImageResource(color);
                break;
            case 2:
                imgFretThree.setImageResource(color);
                break;
            case 3:
                imgFretFour.setImageResource(color);
                break;
            case 4:
                imgFretFive.setImageResource(color);
                break;
            case 5:
                imgFretSix.setImageResource(color);
                break;
            case 6:
                imgFretSeven.setImageResource(color);
                break;
            case 7:
                imgFretOne.setImageResource(color);
                break;
        }
    }

    public class MessageDialog extends AlertDialog.Builder {
        public String[] menuDialog = {"To Learn", "Delete", "Cancel"};
        int cmdPosition;

        protected MessageDialog(Context context, int commandPositionParam) {
            super(context);
            cmdPosition = commandPositionParam;
            this.setTitle("Data Format");
            this.setItems(menuDialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MusicData music = Musics.get(cmdPosition);

                    try {
                        switch (which) {
                            case 0:
                                RmsService.musicManager.DeleteMusic(music);
                                break;
                            case 1:
                                RmsService.musicManager.MusicToLearn(music);
                                break;
                            case 2:
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        FreeStyle.this.showAlertDialog(e.getMessage());
                    }
                }
            });
        }
    }
}
