package com.rmscore.datamodels;

/**
 * Created by Rinaldi on 16/11/2015.
 */
public class NoteData {

    public long ID;
    public long MusicID;
    public int Chord;
    public double Height;
    public long Time = -1;
    public eNoteDirection NoteDirection = eNoteDirection.none;
    public NoteData() {

    }
    public NoteData(NoteData noteParam) {
        ID = noteParam.ID;
        MusicID = noteParam.MusicID;
        Chord = noteParam.Chord;
        Height = noteParam.Height;
        Time = noteParam.Time;
        NoteDirection = noteParam.NoteDirection;
    }

    public int GetDiscreteHeight() {
        int height;

        if (Height >= 0 && Height < 22) {
            height = 0;
        } else if (Height >= 22 && Height < 44) {
            height = 1;
        } else {
            height = 2;
        }

        return height;
    }

    public enum eNoteDirection {
        none, Input, Output
    }
}
