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

    private RMSService rmsService;
    private MusicsDataTable musicsDataTable;
    private NotesDataTable notesDataTable;
    private MusicData musicData;

    private int instrumentSelected = 0;
    private long startMilliseconds;
    private ArrayList<String> instrumentsList = new ArrayList<>();
    private Timer playNoteTimer;
    private MediaPlayer[] mediaPlayer;
    private ArrayList<NoteData> noteTracking = new ArrayList<>(); // to keep track of each note when it's interrupted and leave

    public MusicManager(RMSService rmsServiceParam) {
        rmsService = rmsServiceParam;

        musicsDataTable = new MusicsDataTable(rmsService);
        notesDataTable = new NotesDataTable(rmsService);

        DBTables.add(musicsDataTable);
        DBTables.add(notesDataTable);

        instrumentsList.add("Acoustic Guitar");
        instrumentsList.add("Electric Piano");
        instrumentsList.add("Synth Plunk");
        instrumentsList.add("Trombone");
        instrumentsList.add("Violin");

        LoadInstrument(1);

        for (int i = 0; i < 24; i++) {
            noteTracking.add(new NoteData()); // Add 24 dummy notes
        }
    }

    public void LoadInstrument(int instrumentParam) {
        TypedArray notes;

        instrumentSelected = instrumentParam;

        switch (instrumentParam) {
            case 0:
                notes = rmsService.getResources().obtainTypedArray(R.array.acoustic_guitar_notes);
                break;
            case 1:
                notes = rmsService.getResources().obtainTypedArray(R.array.electric_piano_notes);
                break;
            case 2:
                notes = rmsService.getResources().obtainTypedArray(R.array.synth_pluck_notes);
                break;
            case 3:
                notes = rmsService.getResources().obtainTypedArray(R.array.trombone_notes);
                break;
            case 4:
                notes = rmsService.getResources().obtainTypedArray(R.array.violin_notes);
                break;
            default:
                notes = rmsService.getResources().obtainTypedArray(R.array.electric_piano_notes);
                break;
        }

        mediaPlayer = new MediaPlayer[notes.length()];

        for (int i = 0; i < notes.length(); i++) {
            int k = notes.getResourceId(i, -1);
            if (k != -1)
                this.mediaPlayer[i] = MediaPlayer.create(rmsService, k);
            else
                this.mediaPlayer[i] = null;
        }
    }

    public void onNoteReceived(NoteData noteData) {
        if (noteData.NoteDirection == NoteData.eNoteDirection.Input)
            playNote(noteData);
        else if ((noteData.NoteDirection == NoteData.eNoteDirection.Output) && (NoteShouldLoop())) {
            stopNote(noteData);
        }

        if ((rmsService.CurrentActivity != null) && ((rmsService.CurrentActivity instanceof INoteReceiver))) {
            Message msg = rmsService.CurrentActivity.uiHandler.obtainMessage(1, noteData);
            msg.sendToTarget();
        }

        if ((musicData != null) && (IsRecording)) {
            if (noteData.NoteDirection == NoteData.eNoteDirection.Input) {
                noteTracking.get(noteData.Chord).Chord = noteData.Chord;
                noteTracking.get(noteData.Chord).Height = noteData.Height;
                noteTracking.get(noteData.Chord).StartTime = System.currentTimeMillis() - startMilliseconds;
            } else if ((noteData.NoteDirection == NoteData.eNoteDirection.Output) &&
                    (noteTracking.get(noteData.Chord).StartTime != -1)) {
                noteTracking.get(noteData.Chord).EndTime = System.currentTimeMillis() - startMilliseconds;
                musicData.Notes.add(new NoteData(noteTracking.get(noteData.Chord)));
                noteTracking.get(noteData.Chord).StartTime = -1;
            }
        }
    }

    public void StartRecording() {
        if ((!IsRecording) && (!IsPlaying)) {
            // Create new music
            musicData = new MusicData();
            musicData.Name = "New Music" + musicsDataTable.GetIDForNewMusic();
            musicData.Instrument = instrumentSelected;
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

            LoadInstrument(musicDataParam.Instrument);

            playNoteTimer.schedule(new PlayNoteTimeHandler(notesDataTable.GetNotes(musicDataParam)), 0, 1);
        } else if (IsPlaying) {
            Utils.log("WTF for that");
        }
    }

    public void StopMusic() {
        if (IsPlaying) {
            if ((rmsService.CurrentActivity != null) && ((rmsService.CurrentActivity instanceof INoteReceiver))) {
                NoteData noteData = new NoteData();
                noteData.NoteDirection = NoteData.eNoteDirection.none; // to signal the UI that the music ended
                Message msg = rmsService.CurrentActivity.uiHandler.obtainMessage(1, noteData);
                msg.sendToTarget();
            }
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

    public ArrayList<MusicData> GetMusicsToLearn() {
        return musicsDataTable.GetMusicsToLearn();
    }

    public ArrayList<String> GetStrMusicsToLearn(ArrayList<MusicData> musicsParam) {
        ArrayList<String> m = new ArrayList<>();

        for (int i = 0; i < musicsParam.size(); i++) {
            m.add(musicsParam.get(i).Name);
        }

        return m;
    }

    public ArrayList<String> GetInstruments() {
        return instrumentsList;
    }

    public MusicData GetMusic(long idParam) {
        MusicData musicData = musicsDataTable.GetMusic(idParam);
        musicData.Notes = notesDataTable.GetNotes(musicData);
        return musicData;
    }

    public void DeleteMusic(MusicData musicDataParam) throws Exception {
        musicsDataTable.DeleteMusic(musicDataParam);
    }

    public void MusicToLearn(MusicData musicDataParam) throws Exception {
        musicDataParam.ToLearn = 1;
        musicsDataTable.UpdateMusic(musicDataParam);
    }

    public void MusicNotToLearn(MusicData musicDataParam) throws Exception {
        musicDataParam.ToLearn = 0;
        musicsDataTable.UpdateMusic(musicDataParam);
    }

    public void RenameMusic(MusicData musicDataParam) throws Exception {
        musicsDataTable.UpdateMusic(musicDataParam);
    }

    //return if the instrument hold the note
    private boolean NoteShouldLoop() {
        return (instrumentSelected == 2) || (instrumentSelected == 3) || (instrumentSelected == 4);
    }

    private void playNote(NoteData noteData) {
        MediaPlayer mp = mediaPlayer[(noteData.Chord * 3) + noteData.GetDiscreteHeight()];
        mp.seekTo(0);

//        if (NoteShouldLoop())
//            mp.setLooping(true);

        mp.start();
    }

    private void stopNote(NoteData noteData) {
        mediaPlayer[(noteData.Chord * 3) + noteData.GetDiscreteHeight()].pause();
    }

    public void SendNoteToHarp(NoteData noteData) {
        String strNote = String.valueOf(((char) (noteData.Chord + 'A')));
        if (noteData.NoteDirection == NoteData.eNoteDirection.Input) {
            strNote += "i";
        } else if (noteData.NoteDirection == NoteData.eNoteDirection.Output) {
            strNote += "o";
        } else {
            strNote += "n";
        }
        strNote += String.valueOf(noteData.Height);

        rmsService.SendToBluetooth(strNote + "\r\n");
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
            if (notes.size() > 0) {
                for (int i = 0; i < notes.size(); i++) {
                    if (notes.get(i).StartTime == timeCount) {
                        NoteData noteData = notes.get(i);
                        String noteSend;
                        playNote(noteData);
                        noteSend = String.valueOf(noteData.Chord + ((int) 'A'));
                        noteSend += noteData.Height;
                        rmsService.SendToBluetooth(noteSend + "\r\n");

                        if ((rmsService.CurrentActivity != null) && ((rmsService.CurrentActivity instanceof INoteReceiver))) {
                            noteData.NoteDirection = NoteData.eNoteDirection.Input;
                            Message msg = rmsService.CurrentActivity.uiHandler.obtainMessage(1, noteData);
                            msg.sendToTarget();
                        }
                    }
                    if (notes.get(i).EndTime == timeCount) {
                        if ((rmsService.CurrentActivity != null) && ((rmsService.CurrentActivity instanceof INoteReceiver))) {
                            NoteData noteData = notes.get(i);
                            noteData.NoteDirection = NoteData.eNoteDirection.Output;
                            Message msg = rmsService.CurrentActivity.uiHandler.obtainMessage(1, noteData);
                            msg.sendToTarget();
                        }
                        notes.remove(i);
                    }
                }
            } else {
                if ((rmsService.CurrentActivity != null) && ((rmsService.CurrentActivity instanceof INoteReceiver))) {
                    NoteData noteData = new NoteData();
                    noteData.NoteDirection = NoteData.eNoteDirection.none; // to signal the UI that the music ended
                    Message msg = rmsService.CurrentActivity.uiHandler.obtainMessage(1, noteData);
                    msg.sendToTarget();
                }
                IsPlaying = false;
                this.cancel();
            }
        }
    }

}
