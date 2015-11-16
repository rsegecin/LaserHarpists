package com.rmscore.data;

import android.database.sqlite.SQLiteDatabase;

import com.rmscore.bases.BaseActivity;

import java.util.ArrayList;

public class DBTable {

    public String Name;
    protected BaseActivity baseActivity;
    protected SQLiteDatabase db;
    private ArrayList<DBRegister> Registers;
    private ArrayList<DBTable> ForeignTables;

    public DBTable(BaseActivity baseActivityParam) {
        Registers = new ArrayList<>();
        ForeignTables = new ArrayList<>();
        baseActivity = baseActivityParam;

        db = baseActivity.RmsService.DBManager.getWritableDatabase();
    }

    public DBRegister GetPrimaryKey() {
        DBRegister primary = null;

        for (int i = 0; i < Registers.size(); i++) {
            if (Registers.get(i).isPrimaryKey == true) {
                primary = Registers.get(i);
                break;
            }
        }

        return primary;
    }

    public String GetStringToCreateTable() {
        String sqlQuery = "";
        String placeholder = "CREATE TABLE %s (%s);";
        String strRegisters = "";
        DBRegister tmpReg;

/*			sqlQuery = "CREATE TABLE messageTable  
                        (idMessage INTEGER PRIMARY KEY AUTOINCREMENT,
						Name TEXT, 
						Age Integer, 
					idDept INTEGER NOT NULL,
					FOREIGN KEY idDept REFERENCES deptTable (colDeptID));";
*/

        for (int i = 0; i < Registers.size(); i++) {
            if (i > 0) strRegisters += ", ";

            if (Registers.get(i).isPrimaryKey) {
                strRegisters += Registers.get(i).Name + " " +
                        Registers.get(i).GetStrRegType() + " " +
                        "PRIMARY KEY AUTOINCREMENT";
            } else if (Registers.get(i).isForeignKey) {
                tmpReg = Registers.get(i).ForeignTable.GetPrimaryKey();
                if (tmpReg != null) {
                    strRegisters += tmpReg.Name + " " + tmpReg.GetStrRegType() + ", " +
                            "FOREIGN KEY (" + tmpReg.Name + ") " +
                            "REFERENCES " + Registers.get(i).ForeignTable.Name + " (" + tmpReg.Name + ")";
                }
            } else {
                strRegisters += Registers.get(i).Name + " " + Registers.get(i).GetStrRegType();
            }
        }

        sqlQuery = String.format(placeholder, Name, strRegisters);
        return sqlQuery;
    }

    public void AddRegister(DBRegister dbRegParam) {
        Registers.add(dbRegParam);
    }

    public void AddRegister(DBTable dbTable) {
        dbTable.ForeignTables.add(this);
        DBRegister reg = new DBRegister(dbTable);
        Registers.add(reg);
    }

    public String GetSelectQuery() {
        DBRegister primaryKey = this.GetPrimaryKey();
        String strQuery = "Select";

        for (int i = 0; i < Registers.size(); i++) {
            if (i == 0)
                strQuery += " " + this.Name + "." + Registers.get(i).Name;
            else
                strQuery += ", " + this.Name + "." + Registers.get(i).Name;
        }

        for (int i = 0; i < ForeignTables.size(); i++) {
            for (int j = 0; j < ForeignTables.get(i).Registers.size(); j++) {
                if ((ForeignTables.get(i).Registers.get(j).ForeignTable == null) ||
                        (ForeignTables.get(i).Registers.get(j).ForeignTable.Name != this.Name))
                    strQuery += ", " + ForeignTables.get(i).Name + "." +
                            ForeignTables.get(i).Registers.get(j).Name;
            }
        }

        strQuery += " From " + this.Name;

        for (int i = 0; i < ForeignTables.size(); i++) {
            strQuery += " Left Join " + ForeignTables.get(i).Name + " On ";
            strQuery += this.Name + "." + primaryKey.Name + " = ";
            strQuery += ForeignTables.get(i).Name + "." + primaryKey.Name;
        }

        return strQuery;
    }
}
