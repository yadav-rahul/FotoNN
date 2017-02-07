package com.cfd.rahul.fotonn;

/**
 * Created by anupamchugh on 19/10/15.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }


    //0 for false and 1 for true
    public void insert(String URI, String isTrue) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.URI, URI);
        contentValue.put(DatabaseHelper.IS_FACE, String.valueOf(isTrue));
        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
    }

    public Cursor fetch(String param) {
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.URI, DatabaseHelper.IS_FACE };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns,
                DatabaseHelper.IS_FACE + " = ?", new String[]{param}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String name, String desc) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.URI, name);
        contentValues.put(DatabaseHelper.IS_FACE, desc);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }

}
