package com.philong.androidaudiorecord.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.philong.androidaudiorecord.listeners.OnDatabaseChangeListener;
import com.philong.androidaudiorecord.model.RecordingItem;

import java.util.Comparator;

/**
 * Created by Long on 08/03/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String LOG_TAG = "DBHelper";
    private static OnDatabaseChangeListener onDatabaseChangeListener;
    private static final String DATABASE_NAME = "saved_recordings.db";
    private static final int DATABASE_VERSION  = 2;

    public static abstract class DBHelperItem implements BaseColumns{
        public static final String TABLE_NAME = "saved_recordings";
        public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
        public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
        public static final String COLUMN_NAME_TIME_ADDED = "time_added";
    }

    private static final String CREATE_TABLE_RECORDING = "CREATE TABLE " + DBHelperItem.TABLE_NAME + " ( "
            + DBHelperItem._ID + " INTEGER PRIMARY KEY, "
            + DBHelperItem.COLUMN_NAME_RECORDING_NAME + " TEXT, "
            + DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH + " TEXT, "
            + DBHelperItem.COLUMN_NAME_RECORDING_LENGTH + " INTEGER, "
            + DBHelperItem.COLUMN_NAME_TIME_ADDED + " INTEGER );";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + DBHelperItem.TABLE_NAME;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RECORDING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public static void setOnDatabaseChangeListener(OnDatabaseChangeListener listener){
        onDatabaseChangeListener = listener;
    }

    public RecordingItem getItemAt(int position){
        SQLiteDatabase db = this.getReadableDatabase();
        String [] projection = {
                DBHelperItem._ID,
                DBHelperItem.COLUMN_NAME_RECORDING_NAME,
                DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH,
                DBHelperItem.COLUMN_NAME_RECORDING_LENGTH,
                DBHelperItem.COLUMN_NAME_TIME_ADDED
        };
        Cursor cursor = db.query(DBHelperItem.TABLE_NAME, projection, null, null, null, null, null, null);
        if(cursor.moveToPosition(position)){
            RecordingItem recordingItem = new RecordingItem();
            recordingItem.setId(cursor.getInt(cursor.getColumnIndex(DBHelperItem._ID)));
            recordingItem.setName(cursor.getString(cursor.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_NAME)));
            recordingItem.setFilePath(cursor.getString(cursor.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH)));
            recordingItem.setLength(cursor.getInt(cursor.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH)));
            recordingItem.setTime(cursor.getLong(cursor.getColumnIndex(DBHelperItem.COLUMN_NAME_TIME_ADDED)));
            cursor.close();
            return recordingItem;
        }
        return null;
    }

    public void removeItemWithId(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String [] whereArgs = {String.valueOf(id)};
        db.delete(DBHelperItem.TABLE_NAME, DBHelperItem._ID + " = ?", whereArgs);
    }

    public int getCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {DBHelperItem._ID};
        Cursor cursor = db.query(DBHelperItem.TABLE_NAME, projection, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public class RecordingComparator implements Comparator<RecordingItem>{

        @Override
        public int compare(RecordingItem o1, RecordingItem o2) {
            Long r1 = o1.getTime();
            Long r2 = o2.getTime();
            return r2.compareTo(r1);
        }
    }

    public long addRecording(String recordingName, String filePath, long length){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, recordingName);
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH, length);
        long rowID = db.insert(DBHelperItem.TABLE_NAME, null, values);
        if(onDatabaseChangeListener != null){
            onDatabaseChangeListener.onNewDatabaseEntryAdded();
        }
        return rowID;
    }

    public void renameItem(RecordingItem item, String recordingName, String filePath){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, recordingName);
        values.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        db.update(DBHelperItem.TABLE_NAME, values, DBHelperItem._ID + " = ?", new String[]{String.valueOf(item.getId())});
        if(onDatabaseChangeListener != null){
            onDatabaseChangeListener.onDatabaseEntryRename();
        }
    }
}
