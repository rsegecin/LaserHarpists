package com.rmscore.laserharpists;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rmscore.bases.BaseActivity;
import com.rmscore.datamodels.MusicData;
import com.rmscore.datamodels.NoteData;
import com.rmscore.music.IMusicManager;
import com.rmscore.utils.SimpleArrayAdapter;

import java.util.ArrayList;

public class ScoreGame extends BaseActivity implements IMusicManager {

    private ArrayList<MusicData> musics = new ArrayList<>();
    private ArrayList<String> strMusics = new ArrayList<>();

    private MusicData musicSelected;

    private Spinner spinnerMusics;
    private TextView txtNotesToGo;
    private TextView txtErros;
    private Button btnStartAgain;

    private ImageView imgFretOne;
    private ImageView imgFretTwo;
    private ImageView imgFretThree;
    private ImageView imgFretFour;
    private ImageView imgFretFive;
    private ImageView imgFretSix;
    private ImageView imgFretSeven;
    private ImageView imgFretEight;

    private boolean isLearning = false;
    private int noteIndex = 0;
    private int notesToGo = 0;
    private int noteErrors = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void ServiceStarted() {
        super.ServiceStarted();

        musics = rmsService.musicManager.GetMusicsToLearn();
        strMusics = rmsService.musicManager.GetStrMusicsToLearn(musics);

        InitLayout();
    }

    private void InitLayout() {
        SimpleArrayAdapter adapterMusics =
                new SimpleArrayAdapter(this, R.layout.listview_item,
                        strMusics, SimpleArrayAdapter.eTextAlign.left);

        spinnerMusics = (Spinner) findViewById(R.id.spinnerMusics);
        spinnerMusics.setAdapter(adapterMusics);
        spinnerMusics.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (musics.size() > 0) {
                    musicSelected = rmsService.musicManager.GetMusic(musics.get(position).ID); // Gotten from service to load the notes as well
                } else {
                    showAlertDialog("Please record a music on free style and edit it's properties 'To Learn'");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        txtNotesToGo = (TextView) findViewById(R.id.txtNotesToGo);
        txtErros = (TextView) findViewById(R.id.txtErros);

        btnStartAgain = (Button) findViewById(R.id.btnStartAgain);
        btnStartAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScoreGame.this.StartLearn();
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

        if (strMusics.size() == 0) {
            strMusics.add("[No music to learn]");
        }
    }

    private void StartLearn() {
        isLearning = true;
        noteIndex = 0;
        notesToGo = musicSelected.Notes.size();
        noteErrors = 0;
        txtNotesToGo.setText(String.valueOf(notesToGo));
        txtErros.setText("0");

        SendNoteToHarp();
    }

    private void SendNoteToHarp() {
        rmsService.musicManager.SendNoteToHarp(musicSelected.Notes.get(noteIndex));
        noteIndex++;
    }

    @Override
    public void onNoteReceived(NoteData noteData) {

        if (noteData.NoteDirection == NoteData.eNoteDirection.Input) {
            SetFretImg(noteData.Chord, R.drawable.redfret);
        } else if (noteData.NoteDirection == NoteData.eNoteDirection.Output) {
            SetFretImg(noteData.Chord, R.drawable.blankfret);
        } else {
            ResetAllFrets();
        }

        if (isLearning) {
            notesToGo--;
            txtNotesToGo.setText(String.valueOf(notesToGo));

            if (noteIndex < musicSelected.Notes.size()) {
                if ((noteData.Chord != musicSelected.Notes.get(noteIndex).Chord) ||
                        (noteData.GetDiscreteHeight() != musicSelected.Notes.get(noteIndex).GetDiscreteHeight()) ||
                        (noteData.NoteDirection != musicSelected.Notes.get(noteIndex).NoteDirection)) {
                    noteErrors++;
                    txtErros.setText(String.valueOf(noteErrors));
                }

                SendNoteToHarp();
            } else {
                isLearning = false;
                showAlertDialog("Congrats you finished the music");
            }
        }

    }

    @Override
    public void onMusicStopped() {
        ResetAllFrets();
    }

    private void ResetAllFrets() {
        for (int i = 0; i < 8; i++) {
            SetFretImg(i, R.drawable.blankfret);
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

}
