package com.rmscore.music;

import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Message;
import android.util.Log;

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
    public static final int NUMBER_OF_NOTES = 24;

    public static final int NOTE_RECEIVED = 1;
    public static final int NOTE_PLAYED = 2;
    public static final int MUSIC_STOPPED = 3;

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
//    SoundPool spool;
//    AudioAttributes audioAttributes;

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

        LoadAllNotes();
        LoadInstrument(1);
    }

    public void LoadAllNotes() {
        TypedArray notes = rmsService.getResources().obtainTypedArray(R.array.all_notes);

        mediaPlayer = new MediaPlayer[NUMBER_OF_NOTES * instrumentsList.size()];

        for (int j = 0; j < notes.length(); j++) {
            mediaPlayer[j] = MediaPlayer.create(rmsService, notes.getResourceId(j, -1));
        }

//        audioAttributes = new AudioAttributes.Builder()
//                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                .setUsage(AudioAttributes.USAGE_GAME)
//                .build();
//
//        spool = new SoundPool.Builder()
//                .setMaxStreams(NUMBER_OF_NOTES)
//                .setAudioAttributes(audioAttributes)
//                .build();
//
//        for (int j = 0; j < notes.length(); j++) {
//            spool.load(rmsService, notes.getResourceId(j, -1), 1);
//        }
    }

    public void LoadInstrument(int instrumentParam) {
        instrumentSelected = instrumentParam;
    }

    public void onNoteReceived(NoteData noteData) {
        if (noteData.NoteDirection == NoteData.eNoteDirection.Input) {
            playNote(noteData);
        } else if ((noteData.NoteDirection == NoteData.eNoteDirection.Output) && (noteShouldLoop())) {
            stopNote(noteData);
        }

        if ((rmsService.CurrentActivity != null) && ((rmsService.CurrentActivity instanceof IMusicManager))) {
            Message msg = rmsService.CurrentActivity.uiHandler.obtainMessage(NOTE_RECEIVED, noteData);
            msg.sendToTarget();
        }

        if ((musicData != null) && (IsRecording)) {
            long time = System.currentTimeMillis() - startMilliseconds;
            long discreteTime = time / 40;
            discreteTime *= 40;
            noteData.Time = discreteTime;
            musicData.Notes.add(noteData);
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

            playNoteTimer.schedule(new PlayNoteTimeHandler(notesDataTable.GetNotes(musicDataParam)), 0, 20);
        } else if (IsPlaying) {
            Utils.log("WTF for that");
        }
    }

    public void StopMusic() {
        if (IsPlaying) {
            NotifyUiMusicStopped();
            IsPlaying = false;
            playNoteTimer.cancel();
            playNoteTimer.purge();
        }
    }

    public void NotifyUiMusicStopped() {
        if ((rmsService.CurrentActivity != null) && ((rmsService.CurrentActivity instanceof IMusicManager))) {
            Message msg = rmsService.CurrentActivity.uiHandler.obtainMessage(MUSIC_STOPPED);
            msg.sendToTarget();
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
    private boolean noteShouldLoop() {
        return (instrumentSelected == 2) || (instrumentSelected == 3) || (instrumentSelected == 4);
    }

    private void playNote(NoteData noteData) {
        int note = (noteData.Chord * 3) + noteData.GetDiscreteHeight();
        note += (instrumentSelected * NUMBER_OF_NOTES) + 1;
        mediaPlayer[note].seekTo(0);
        mediaPlayer[note].start();
        //spool.play(note, 1, 1, 1, 0, 1);
        Log.d("LaserHarpists", "Instrument " + instrumentSelected + " note playing " + note);
    }

    private void stopNote(NoteData noteData) {
        //mediaPlayer[instrumentSelected][(noteData.Chord * 3) + noteData.GetDiscreteHeight()].pause();
        //mediaPlayer[instrumentSelected][(noteData.Chord * 3) + noteData.GetDiscreteHeight()].release();
    }

    public synchronized void SendNoteToHarp(NoteData noteData) {
        char chare;

        if (noteData.NoteDirection == NoteData.eNoteDirection.Input) {
            chare = (char) ((noteData.Chord * 3) + noteData.GetDiscreteHeight() + 'A');
        } else {
            chare = (char) (noteData.Chord + '0');
        }

        rmsService.SendToBluetooth(chare + "\r\n");

        Log.d("LaserHarpists", "Sending note " + chare);
    }

    public class PlayNoteTimeHandler extends TimerTask {
        ArrayList<NoteData> notes;
        long timeCount = 0;

        public PlayNoteTimeHandler(ArrayList<NoteData> notesParam) {
            notes = notesParam;
        }

        @Override
        public void run() {
            int notesSize = notes.size();
            timeCount += 20;
            if (notesSize > 0) {
                int max = (notesSize < 8) ? notesSize : 8;
                int i = 0;

                do {
                    if (notes.get(i).Time == timeCount) {
                        NoteData noteData = notes.get(i);

                        SendNoteToHarp(noteData);

                        if (noteData.NoteDirection == NoteData.eNoteDirection.Input) {
                            playNote(noteData);
                        } else if (noteData.NoteDirection == NoteData.eNoteDirection.Output) {
                            stopNote(noteData);
                        }

                        if ((rmsService.CurrentActivity != null) && ((rmsService.CurrentActivity instanceof IMusicManager))) {
                            Message msg = rmsService.CurrentActivity.uiHandler.obtainMessage(NOTE_PLAYED, noteData);
                            msg.sendToTarget();
                        }
                        notes.remove(i);
                    } else {
                        i++;
                    }
                } while ((i < 8) && (0 < notes.size()) && (i < notes.size()));
            } else {
                IsPlaying = false;
                NotifyUiMusicStopped();
                this.cancel();
            }
        }
    }

}
