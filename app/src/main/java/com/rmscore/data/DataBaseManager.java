package com.rmscore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBaseManager extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "RmsDB";

    private ArrayList<DBTable> DBTables;

    public DataBaseManager(Context context, ArrayList<DBTable> tablesParam) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        DBTables = tablesParam;

        //context.deleteDatabase(DATABASE_NAME);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        for (DBTable table : DBTables) {
            db.execSQL(table.GetStringToCreateTable());
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
    }

    public DBTable GetTableByName(String nameParam) {
        DBTable tmpDBTable = null;
        for (DBTable dbTable : DBTables) {
            if (dbTable.Name.equals(nameParam)) {
                tmpDBTable = dbTable;
            }
        }
        return tmpDBTable;
    }
}