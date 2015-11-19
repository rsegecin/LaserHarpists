package com.rmscore;

import android.content.res.TypedArray;
import android.media.MediaPlayer;

import com.rmscore.data.DBTable;
import com.rmscore.databasemodels.MusicsDataTable;
import com.rmscore.databasemodels.NotesDataTable;
import com.rmscore.datamodels.MusicData;
import com.rmscore.datamodels.NoteData;
import com.rmscore.laserharpists.R;
import com.rmscore.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Rinaldi on 19/11/2015.
 */
public class MusicManager {

    public ArrayList<DBTable> DBTables = new ArrayList<>();
    public boolean IsRecording = false;
    private RMSService rmsService;
    private MusicsDataTable musicsDataTable;
    private NotesDataTable notesDataTable;
    private MusicData musicData;

    private MediaPlayer[] key = new MediaPlayer[19];

    private long startMilliseconds;

    public MusicManager(RMSService rmsServiceParam) {
        rmsService = rmsServiceParam;

        musicsDataTable = new MusicsDataTable(rmsService);
        notesDataTable = new NotesDataTable(rmsService);

        DBTables.add(musicsDataTable);
        DBTables.add(notesDataTable);

        TypedArray notes = rmsService.getResources().obtainTypedArray(R.array.notes);
        for (int i = 0; i < notes.length(); i++) {
            int k = notes.getResourceId(i, -1);
            if (k != -1) {
                this.key[i] = MediaPlayer.create(rmsService, k);
            } else this.key[i] = null;
        }
    }

    public void StartRecording(int instrumentChosenParam) {
        if (!IsRecording) {
            // Create new music
            musicData = new MusicData();
            musicData.Name = "New Music" + musicsDataTable.GetIDForNewMusic();
            musicData.Instrument = instrumentChosenParam;
            musicData.Notes = new ArrayList<>();

            startMilliseconds = System.currentTimeMillis();
            IsRecording = true;
        } else {
            Utils.log("WTF for that");
        }
    }

    public void StopRecording() {
        if (musicData != null) {
            try {
                musicsDataTable.AddMusic(musicData);
                musicData.Notes.clear();
                musicData = null;
            } catch (Exception e) {
                e.printStackTrace();
                if (rmsService.CurrentActivity != null) {
                    rmsService.CurrentActivity.showAlertDialog(e.getMessage());
                }
            }
        }

        IsRecording = false;
    }

    public void onNoteReceived(NoteData noteData) {
        if ((IsRecording) && (musicData != null)) {
            noteData.StartTime = System.currentTimeMillis() - startMilliseconds;
            musicData.Notes.add(noteData);

            //TODO: Tie with user interface.
            //TODO: Depending on the instrument chosen play different mid
            playNote(key[noteData.Chord]);
        }
    }

    private void playNote(MediaPlayer mp) {
        mp.seekTo(0);
        mp.start();
    }
}
