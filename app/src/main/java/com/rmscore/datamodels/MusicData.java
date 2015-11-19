package com.rmscore.datamodels;

import java.util.ArrayList;

/**
 * Created by Rinaldi on 16/11/2015.
 */
public class MusicData {
    public int ID;
    public String Name;
    public String Author;
    public String AuthorsBestScore;
    public double BestScore;
    public int Instrument;
    public int ToLearn;

    public ArrayList<NoteData> Notes = new ArrayList<>();

}
