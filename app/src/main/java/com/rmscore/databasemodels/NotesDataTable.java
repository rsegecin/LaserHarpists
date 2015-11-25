package com.rmscore.databasemodels;

import android.content.ContentValues;
import android.database.Cursor;

import com.rmscore.RMSService;
import com.rmscore.data.DBRegister;
import com.rmscore.data.DBTable;
import com.rmscore.datamodels.MusicData;
import com.rmscore.datamodels.NoteData;

import java.util.ArrayList;

/**
 * Created by Rinaldi on 16/11/2015.
 */
public class NotesDataTable extends DBTable {

    public NotesDataTable(RMSService rmsServiceParam) {
        super(rmsServiceParam);
        Name = "Notes";

        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "id_note", true));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "id_music"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "chord"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.REAL, "height"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "time"));
        AddRegister(new DBRegister(DBRegister.eRegisterTypes.INTEGER, "direction"));
    }

    public void AddNote(NoteData noteDataParam) throws Exception {
        ContentValues cv;

        if (noteDataParam.MusicID != 0) {
            cv = new ContentValues();
            cv.put("id_music", noteDataParam.MusicID);
            cv.put("chord", noteDataParam.Chord);
            cv.put("height", noteDataParam.Height);
            cv.put("time", noteDataParam.Time);
            cv.put("direction", noteDataParam.NoteDirection.ordinal());
            db.insert(Name, null, cv);
        } else {
            throw new Exception("The Note must belong to some music.");
        }
    }

    public void DeleteNotes(MusicData musicDataParam) throws Exception {
        db.delete(Name, "id_music = ?", new String[]{String.valueOf(musicDataParam.ID)});
    }

    public ArrayList<NoteData> GetNotes(MusicData musicDataParam) {
        ArrayList<NoteData> notes = new ArrayList<>();
        Cursor cursor = db.query(Name, null, "id_music = ?", new String[]{String.valueOf(musicDataParam.ID)}, null, null, null);

        if ((cursor != null) && (cursor.moveToFirst())) {
            do {
                NoteData note = new NoteData();
                note.ID = Integer.valueOf(cursor.getString(0));
                note.MusicID = Integer.valueOf(cursor.getString(1));
                note.Chord = Integer.valueOf(cursor.getString(2));
                note.Height = Double.valueOf(cursor.getString(3));
                note.Time = Integer.valueOf(cursor.getString(4));
                note.NoteDirection = NoteData.eNoteDirection.values()[Integer.valueOf(cursor.getString(5))];
                notes.add(note);
            } while (cursor.moveToNext());
        }

        return notes;
    }
}
