package com.rmscore.laserharpists;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
    private Button btnEdit;
    private Button btnMusicAction;
    private Button btnRecord;
    private Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_style);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void ServiceStarted() {
        super.ServiceStarted();

        InitLayout();
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
                FreeStyle.this.RmsService.musicManager.InstrumentSelected = position;
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

        btnEdit = (Button) findViewById(R.id.btnEdit);
        ButtonSetColor(R.id.btnEdit, Color.LTGRAY);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerRecords.getSelectedItemPosition() > 0) {
                    MusicData music = Musics.get(spinnerRecords.getSelectedItemPosition() - 1);
                    MessageDialog messageDialog = new MessageDialog(FreeStyle.this, music);
                    messageDialog.show();
                } else {
                    showAlertDialog("Select a music to be edited.");
                }
            }
        });

        btnMusicAction = (Button) findViewById(R.id.btnMusicAction);
        ButtonSetColor(R.id.btnMusicAction, Color.LTGRAY);
        btnMusicAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!RmsService.musicManager.IsRecording) {
                    if (spinnerRecords.getSelectedItemPosition() > 0) {
                        if (!RmsService.musicManager.IsPlaying) {
                            MusicData music = Musics.get(spinnerRecords.getSelectedItemPosition() - 1); // Because add "Musics Recorded" in the first line
                            RmsService.musicManager.PlayMusic(music);
                            chronometer.start();
                            btnMusicAction.setText("Stop");
                        } else {
                            RmsService.musicManager.StopMusic();
                            btnMusicAction.setText("Play");
                            chronometer.reset();
                        }
                    } else {
                        showAlertDialog("Select a music to be played.");
                    }
                } else {
                    showAlertDialog("Cannot play music while recording.");
                }
            }
        });

        btnRecord = (Button) findViewById(R.id.btnRecord);
        ButtonSetColor(R.id.btnRecord, Color.LTGRAY);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!RmsService.musicManager.IsPlaying) {
                    if (!RmsService.musicManager.IsRecording) {
                        RmsService.musicManager.StartRecording(spinnerSoundType.getSelectedItemPosition());
                        btnRecord.setText("Recording");
                        ButtonSetColor(R.id.btnRecord, 0xFFDF3831);
                        chronometer.start();
                    } else {
                        try {
                            RmsService.musicManager.StopRecording();
                        } catch (Exception e) {
                            FreeStyle.this.showAlertDialog(e.getMessage());
                        }
                        btnRecord.setText("Record");
                        ButtonSetColor(R.id.btnRecord, Color.LTGRAY);
                        chronometer.reset();

                        UpdateSpinnerRecords();
                    }
                } else {
                    showAlertDialog("Cannot record music while playing.");
                }
            }
        });

    }

    private void ResetAllFrets() {
        for (int i = 0; i < 8; i++) {
            SetFretImg(i, R.drawable.blankfret);
        }
    }

    public void ButtonSetColor(int buttonName, int color) {
        Drawable d = findViewById(buttonName).getBackground();
        PorterDuffColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        d.setColorFilter(filter);
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
    }

    @Override
    public void onNoteReceived(NoteData noteData) {

        if (noteData.NoteDirection == NoteData.eNoteDirection.Input) {
            SetFretImg(noteData.Chord, R.drawable.redfret);
        } else if (noteData.NoteDirection == NoteData.eNoteDirection.Output) {
            SetFretImg(noteData.Chord, R.drawable.blankfret);
        } else {
            ResetAllFrets();
            chronometer.reset();
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
                imgFretEight.setImageResource(color);
                break;
        }
    }

    private void showConfirmationDialog(final MusicData music) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Music")
                .setMessage("Do you really want to delete the music " + music.Name + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            RmsService.musicManager.DeleteMusic(music);
                            showAlertDialog("Music " + music.Name + " was deleted.");
                            UpdateSpinnerRecords();
                        } catch (Exception e) {
                            showAlertDialog(e.getMessage());
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void showRenameMusicDialog(final MusicData musicParam) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Music");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    musicParam.Name = input.getText().toString();
                    RmsService.musicManager.RenameMusic(musicParam);
                    UpdateSpinnerRecords();
                } catch (Exception e) {
                    showAlertDialog(e.getMessage());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public class MessageDialog extends AlertDialog.Builder {
        public String[] menuDialog = {"To Learn", "Delete", "Rename", "Cancel"};
        MusicData music;

        protected MessageDialog(Context context, final MusicData musicParam) {
            super(context);
            music = musicParam;
            this.setTitle("Data Format");

            if (musicParam.ToLearn == 1) menuDialog[0] = "Not to Learn";

            this.setItems(menuDialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        switch (which) {
                            case 0:
                                if (musicParam.ToLearn == 0) {
                                    RmsService.musicManager.MusicToLearn(music);
                                    showAlertDialog("Music set to learn.");
                                } else {
                                    RmsService.musicManager.MusicNotToLearn(music);
                                    showAlertDialog("Music set not to learn.");
                                }
                                break;
                            case 1:
                                showConfirmationDialog(music);
                                break;
                            case 2:
                                showRenameMusicDialog(music);
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
