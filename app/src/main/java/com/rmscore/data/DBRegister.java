package com.rmscore.data;

public class DBRegister {
    public eRegisterTypes RegisterType;
    public String Name;
    public boolean isPrimaryKey = false;
    public boolean isForeignKey = false;
    public DBTable ForeignTable;

    public DBRegister(eRegisterTypes regTypeParam, String nameParam) {
        RegisterType = regTypeParam;
        Name = nameParam;
    }

    public DBRegister(eRegisterTypes regTypeParam, String nameParam, boolean isPrimaryKeyParam) {
        RegisterType = regTypeParam;
        Name = nameParam;
        isPrimaryKey = isPrimaryKeyParam;
    }

    public DBRegister(DBTable foreignTableParam) {
        isForeignKey = true;
        ForeignTable = foreignTableParam;
    }

    public String GetStrRegType() {
        switch (this.RegisterType) {
            case NULL:
                return "NULL";
            case INTEGER:
                return "INTEGER";
            case REAL:
                return "REAL";
            case TEXT:
                return "TEXT";
            case BLOB:
                return "BLOB";
            default:
                return "";
        }
    }

    public enum eRegisterTypes {
        NULL, INTEGER, REAL, TEXT, BLOB
    }
}