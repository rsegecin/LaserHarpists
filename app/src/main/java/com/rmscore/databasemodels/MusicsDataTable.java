package com.rmscore.databasemodels;

import android.content.ContentValues;
import android.database.Cursor;

import com.rmscore.RMSService;
import com.rmscore.data.DBRegister;
import com.rmscore.data.DBTable;
import com.rmscore.datamodels.MusicData;

import java.util.ArrayList;

/**
 * Created by Rinaldi on 16/11/2015.
 */
public class MusicsDataTable extends DBTable {

    public MusicsDataTable(RMSService rmsServiceParam) {
        super(rmsServiceParam);
        Name = "Music";

        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "id_music", true));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.TEXT, "name"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.TEXT, "author"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "instrument"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "to_learn"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.TEXT, "author_best_score"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "best_score"));
    }

    public long AddMusic(MusicData musicDataParam) throws Exception {
        ContentValues cv;
        long lastId;

        if (!musicDataParam.Name.isEmpty()) {
            if (musicDataParam.Notes.size() > 0) {
                cv = new ContentValues();
                cv.put("name", musicDataParam.Name);
                cv.put("author", musicDataParam.Author);
                cv.put("instrument", musicDataParam.Instrument);
                cv.put("to_learn", musicDataParam.ToLearn);
                lastId = db.insert(Name, null, cv);
            } else {
                throw new Exception("The music hasn't any notes.");
            }
        } else {
            throw new Exception("Insert music name, who's the author to create music and the instrument played.");
        }

        return lastId;
    }

    public void DeleteMusic(MusicData musicDataParam) throws Exception {
        db.delete(Name, "id_music = ?", new String[]{String.valueOf(musicDataParam.ID)});
    }

    public void UpdateMusic(MusicData musicDataParam) throws Exception {
        ContentValues cv;

        if ((!musicDataParam.Name.isEmpty()) && (!musicDataParam.Author.isEmpty()) && (musicDataParam.Instrument != 0)) {
            cv = new ContentValues();
            cv.put("name", musicDataParam.Name);
            cv.put("author", musicDataParam.Author);
            cv.put("instrument", musicDataParam.Instrument);
            cv.put("author_best_score", musicDataParam.AuthorsBestScore);
            cv.put("best_score", musicDataParam.BestScore);
            cv.put("to_learn", musicDataParam.ToLearn);
            db.update(Name, cv, "id_music=?",
                    new String[]{String.valueOf(musicDataParam.ID)});
        } else {
            throw new Exception("Insert music name, who's the author to create music and the instrument played.");
        }
    }

    public ArrayList<MusicData> GetMusics() {
        ArrayList<MusicData> musics = new ArrayList<>();
        Cursor cursor = db.rawQuery(rmsService.DBManager.GetTableByName(Name).GetSelectQuery(), null);

        if ((cursor != null) && (cursor.moveToFirst())) {
            do {
                MusicData music = new MusicData();
                music.ID = Integer.valueOf(cursor.getString(0));
                music.Name = cursor.getString(1);
                music.Author = cursor.getString(2);
                music.Instrument = Integer.valueOf(cursor.getString(3));
                music.ToLearn = Integer.valueOf(cursor.getString(4));
                music.AuthorsBestScore = cursor.getString(5);
                if ((cursor.getString(6) != null) && (!cursor.getString(6).isEmpty()))
                    music.BestScore = Double.valueOf(cursor.getString(6));
                musics.add(music);
            } while (cursor.moveToNext());
        }

        return musics;
    }

    public ArrayList<MusicData> GetMusicsToLearn() {
        ArrayList<MusicData> musics = new ArrayList<>();
        Cursor cursor = db.rawQuery("Select * From " + Name + " Where to_learn = 1", null);

        if ((cursor != null) && (cursor.moveToFirst())) {
            do {
                MusicData music = new MusicData();
                music.ID = Integer.valueOf(cursor.getString(0));
                music.Name = cursor.getString(1);
                music.Author = cursor.getString(2);
                music.Instrument = Integer.valueOf(cursor.getString(3));
                music.ToLearn = Integer.valueOf(cursor.getString(4));
                music.AuthorsBestScore = cursor.getString(5);
                music.BestScore = Double.valueOf(cursor.getString(6));
                musics.add(music);
            } while (cursor.moveToNext());
        }

        return musics;
    }

    public ArrayList<String> GetMusicList() {
        ArrayList<MusicData> musics = this.GetMusics();
        ArrayList<String> strMusics = new ArrayList<>();

        for (MusicData music : musics) {
            strMusics.add(music.Name);
        }

        return strMusics;
    }

    public int GetIDForNewMusic() {
        Cursor cursor = db.rawQuery("Select Max(id_music) from " + Name, null);

        if ((cursor != null) && (cursor.moveToFirst())) {
            if (cursor.getString(0) != null)
                return Integer.valueOf(cursor.getString(0)) + 1;
        }

        return 1;
    }
}
