package com.rmscore.datamodels;

/**
 * Created by Rinaldi on 16/11/2015.
 */
public class NoteData {
    public long ID;
    public long MusicID;
    public int Chord;
    public double Height;
    public long StartTime = -1;
    public long EndTime;
    public eNoteDirection NoteDirection = eNoteDirection.none;

    public NoteData() {

    }

    public NoteData(NoteData noteParam) {
        ID = noteParam.ID;
        MusicID = noteParam.MusicID;
        Chord = noteParam.Chord;
        Height = noteParam.Height;
        StartTime = noteParam.StartTime;
        EndTime = noteParam.EndTime;
        NoteDirection = noteParam.NoteDirection;
    }

    public enum eNoteDirection {
        none, Input, Output
    }
}
