package com.rmscore.music;

import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Message;

import com.rmscore.RMSService;
import com.rmscore.data.DBTable;
import com.rmscore.databasemodels.MusicsDataTable;
import com.rmscore.databasemodels.NotesDataTable;
import com.rmscore.datamodels.MusicData;
import com.rmscore.datamodels.NoteData;
import com.rmscore.laserharpists.R;
import com.rmscore.utils.Utils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Rinaldi on 19/11/2015.
 */
public class MusicManager {

    public ArrayList<DBTable> DBTables = new ArrayList<>();
    public boolean IsRecording = false;
    public boolean IsPlaying = false;
    public Timer playNoteTimer;
    private ArrayList<String> Instruments = new ArrayList<>();
    private RMSService rmsService;
    private MusicsDataTable musicsDataTable;
    private NotesDataTable notesDataTable;
    private MusicData musicData;
    private ArrayList<NoteData> NoteTracking = new ArrayList<>(); // to keep track of each note when it's interrupted and leave
    private MediaPlayer[] key = new MediaPlayer[19];
    private long startMilliseconds;

    public MusicManager(RMSService rmsServiceParam) {
        rmsService = rmsServiceParam;

        musicsDataTable = new MusicsDataTable(rmsService);
        notesDataTable = new NotesDataTable(rmsService);

        DBTables.add(musicsDataTable);
        DBTables.add(notesDataTable);

        Instruments.add("Piano");

        TypedArray notes = rmsService.getResources().obtainTypedArray(R.array.notes);
        for (int i = 0; i < notes.length(); i++) {
            int k = notes.getResourceId(i, -1);
            if (k != -1) {
                this.key[i] = MediaPlayer.create(rmsService, k);
            } else this.key[i] = null;
        }

        for (int i = 0; i < 8; i++) {
            NoteTracking.add(new NoteData()); // Add 8 dummy notes
        }
    }

    public void onNoteReceived(NoteData noteData) {

        if ((IsRecording) && (musicData != null)) {
            if (noteData.NoteDirection == NoteData.eNoteDirection.Input) {
                NoteTracking.get(noteData.Chord).Chord = noteData.Chord;
                NoteTracking.get(noteData.Chord).Height = noteData.Height;
                NoteTracking.get(noteData.Chord).StartTime = System.currentTimeMillis() - startMilliseconds;
                playNote(noteData);
            } else if ((noteData.NoteDirection == NoteData.eNoteDirection.Output) &&
                    (NoteTracking.get(noteData.Chord).StartTime != -1)) {
                NoteTracking.get(noteData.Chord).EndTime = System.currentTimeMillis() - startMilliseconds;
                musicData.Notes.add(new NoteData(NoteTracking.get(noteData.Chord)));
                NoteTracking.get(noteData.Chord).StartTime = -1;
            }
            if ((rmsService.CurrentActivity != null) && ((rmsService.CurrentActivity instanceof INoteReceiver))) {
                Message msg = rmsService.CurrentActivity.uiHandler.obtainMessage(1, noteData);
                msg.sendToTarget();
            }
        }

    }

    public void StartRecording(int instrumentChosenParam) {
        if ((!IsRecording) && (!IsPlaying)) {
            // Create new music
            musicData = new MusicData();
            musicData.Name = "New Music" + musicsDataTable.GetIDForNewMusic();
            musicData.Instrument = instrumentChosenParam;
            musicData.Notes = new ArrayList<>();

            startMilliseconds = System.currentTimeMillis();
            IsRecording = true;
        } else if (IsRecording) {
            Utils.log("WTF for that");
        }
    }

    public void StopRecording() throws Exception {
        IsRecording = false;

        if (musicData != null) {
            long music_id = musicsDataTable.AddMusic(musicData);

            for (int i = 0; i < musicData.Notes.size(); i++) {
                musicData.Notes.get(i).MusicID = music_id;
                notesDataTable.AddNote(musicData.Notes.get(i));
            }

            musicData.Notes.clear();
            musicData = null;
        }
    }

    public void PlayMusic(MusicData musicDataParam) {
        if ((!IsPlaying) && (!IsRecording)) {
            IsPlaying = true;
            playNoteTimer = new Timer();
            playNoteTimer.schedule(new PlayNoteTimeHandler(notesDataTable.GetNotes(musicDataParam)), 0, 1);
        } else if (IsPlaying) {
            Utils.log("WTF for that");
        }
    }

    public void StopMusic() {
        if (IsPlaying) {
            IsPlaying = false;
            playNoteTimer.cancel();
            playNoteTimer.purge();
        }
    }

    public ArrayList<MusicData> GetMusics() {
        return musicsDataTable.GetMusics();
    }

    public ArrayList<String> GetStrMusics(ArrayList<MusicData> musicsParam) {
        ArrayList<String> m = new ArrayList<>();

        for (int i = 0; i < musicsParam.size(); i++) {
            m.add(musicsParam.get(i).Name);
        }

        return m;
    }

    public ArrayList<String> GetInstruments() {
        return Instruments;
    }

    public void DeleteMusic(MusicData musicDataParam) throws Exception {
        musicsDataTable.DeleteMusic(musicDataParam);
    }

    public void MusicToLearn(MusicData musicDataParam) throws Exception {
        musicDataParam.ToLearn = 1;
        musicsDataTable.UpdateMusic(musicDataParam);
    }

    public ArrayList<MusicData> GetMusicsToLearn() {
        return musicsDataTable.GetMusicsToLearn();
    }

    //TODO: Depending on the instrument chosen play different mid also depending on the height change the note played
    private void playNote(NoteData noteData) {
        MediaPlayer mp = key[noteData.Chord];
        mp.seekTo(0);
        mp.start();
    }

    public class PlayNoteTimeHandler extends TimerTask {
        ArrayList<NoteData> notes;
        long timeCount = 0;

        public PlayNoteTimeHandler(ArrayList<NoteData> notesParam) {
            notes = notesParam;
        }

        @Override
        public void run() {
            timeCount++;
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).StartTime == timeCount) {
                    playNote(notes.get(i));
                }
            }
        }
    }
}
