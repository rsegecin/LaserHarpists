package com.rmscore.datamodels;

import android.content.ContentValues;
import android.database.Cursor;

import com.rmscore.bases.BaseActivity;
import com.rmscore.data.DBRegister;
import com.rmscore.data.DBTable;

import java.util.ArrayList;

/**
 * Created by Rinaldi on 16/11/2015.
 */
public class MusicsDataTable extends DBTable {

    public MusicsDataTable(BaseActivity baseActivityParam) {
        super(baseActivityParam);
        Name = "Music";

        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "id_music", true));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.TEXT, "name"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.TEXT, "author"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "instrument"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.TEXT, "author_best_score"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "best_score"));
    }

    public void AddMusic(MusicData musicDataParam) throws Exception {
        ContentValues cv;

        if ((!musicDataParam.Name.isEmpty()) && (!musicDataParam.Author.isEmpty()) && (musicDataParam.Instrument != 0)) {
            cv = new ContentValues();
            cv.put("name", musicDataParam.Name);
            cv.put("author", musicDataParam.Author);
            cv.put("instrument", musicDataParam.Instrument);
            db.insert(Name, null, cv);

            db.close();
        } else {
            throw new Exception("Insert music name, who's the author to create music and the instrument played.");
        }
    }

    public void DeleteMusic(MusicData musicDataParam) throws Exception {
        db.delete(Name, "id_music = ?", new String[]{String.valueOf(musicDataParam.ID)});
        db.close();
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
            db.update(Name, cv, "id_music=?",
                    new String[]{String.valueOf(musicDataParam.ID)});

            db.close();
        } else {
            throw new Exception("Insert music name, who's the author to create music and the instrument played.");
        }
    }

    public ArrayList<MusicData> GetMusics() {
        ArrayList<MusicData> musics = new ArrayList<MusicData>();
        Cursor cursor = db.rawQuery(baseActivity.RmsService.DBManager.GetTableByName(Name).GetSelectQuery(), null);

        if ((cursor != null) && (cursor.moveToFirst())) {
            do {
                MusicData music = new MusicData();
                music.ID = Integer.valueOf(cursor.getString(0));
                music.Name = cursor.getString(1);
                music.Author = cursor.getString(2);
                music.Instrument = Integer.valueOf(cursor.getString(3));
                music.AuthorsBestScore = cursor.getString(4);
                music.BestScore = Double.valueOf(cursor.getString(5));
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
}
