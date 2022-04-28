package com.mounirgaiby.textify.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, "settings.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table settings(uid TEXT primary key,visibility INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {


    }

    public Boolean insertDataSettings(String uid,int visibility){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uid",uid);
        contentValues.put("visibility",visibility);
        Cursor cursor = db.rawQuery("select * from settings where uid = ?",new String[] {uid});
        if(cursor.getCount()>0){return false;}else{
        long result = db.insert("settings",null,contentValues);
        if(result==-1){
            return false;
        }else{
            return true;
        }}
    }
    public Boolean updateDataSettings(String uid,int visibility){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uid",uid);
        contentValues.put("visibility",visibility);
        Cursor cursor = db.rawQuery("select * from settings where uid = ?",new String[] {uid});
        if(cursor.getCount()>0){
        long result = db.update("settings",contentValues,"uid=?",new String[] {uid});
        if(result==-1){
            return false;
        }else{
            return true;
        }}else{
            return false;
        }
    }

    public Boolean deleteDataSettings(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from settings where uid = ?",new String[] {uid});
        if(cursor.getCount()>0){
        long result = db.delete("settings","uid = ?",new String[] {uid});
        if(result==-1){
            return false;
        }else{
            return true;
        }}else{
            return false;
        }
    }

    public Cursor getData(String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from settings where uid = ?",new String[] {uid});
        return cursor;
    }





}
