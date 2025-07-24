package com.example.classtable.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.classtable.bean.Notify;
import com.example.classtable.bean.TableCell;

import java.util.ArrayList;
import java.util.List;

public class NotifyDBHelper extends SQLiteOpenHelper {
    private static NotifyDBHelper Helper;
    private static SQLiteDatabase Write;
    private static SQLiteDatabase Read;
    private static String DB_NAME = "notify.db";
    private static String NOTIFY = "notify";

    public NotifyDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table notify(" +
                "id integer primary key autoincrement," +
                "info text," +
                "switch_on integer," +
                "ifVibrate integer," +
                "ifSound integer," +
                "time text," +
                "creat_time)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 删除旧表
        db.execSQL("DROP TABLE IF EXISTS notify");

        // 重新创建表
        onCreate(db);
    }

    public static NotifyDBHelper getInstance(Context context) {
        if (Helper == null) {
            Helper = new NotifyDBHelper(context);
        }
        return Helper;
    }


    public SQLiteDatabase openWrite() {
        if (Write == null || !Write.isOpen()) {
            Write = Helper.getWritableDatabase();
        }
        return Write;
    }

    public SQLiteDatabase openRead() {
        if (Read == null || !Read.isOpen()) {
            Read = Helper.getReadableDatabase();
        }
        return Read;
    }

    public void closelink() {
        if (Read != null && Read.isOpen()) {
            Read.close();
            Read = null;//利于系统回收
        }
        if (Write != null && Write.isOpen()) {
            Write.close();
            Write = null;//利于系统回收
        }
    }

    public List<Notify> queryall() {
        String sql = "SELECT * FROM " + NOTIFY;
        List<Notify> list = new ArrayList<>();
        Cursor cursor = Read.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            Notify notify = new Notify();
            notify.setId(cursor.getInt(0));
            notify.setInfo(cursor.getString(1));
            notify.setSwitch_on(cursor.getInt(2));
            notify.setIfVibrate(cursor.getInt(3));
            notify.setIfSound(cursor.getInt(4));
            notify.setTime(cursor.getString(5));
            notify.setCreat_time(cursor.getString(6));
            list.add(notify);
        }
        return list;
    }

    public Cursor change_switch(int id, int switch_on) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("switch_on", switch_on);
        Write.update(NOTIFY, contentValues, "id=?", new String[]{String.valueOf(id)});
        // 查询所有记录
        return Read.query(NOTIFY, null, null, null, null, null, null);
    }

    public void add_notify(Notify notify) {
        ContentValues values = new ContentValues();
        values.put("time", notify.getTime());
        values.put("info", notify.getInfo());
        values.put("ifVibrate", notify.getIfVibrate());
        values.put("switch_on", notify.getSwitch_on());
        values.put("ifSound", notify.getIfSound());
        values.put("creat_time",notify.getCreat_time());
        Write.insert(NOTIFY, null, values);
    }

    public void delete_notify(int id) {
        Write.delete(NOTIFY, "id=?", new String[]{String.valueOf(id)});
    }
    public Notify queryOne(int id) {
        Notify notify = null;
        Cursor cursor = Read.query(NOTIFY, null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToNext()) {
            notify = new Notify();
            notify.setId(cursor.getInt(0));
            notify.setInfo(cursor.getString(1));
            notify.setSwitch_on(cursor.getInt(2));
            notify.setIfVibrate(cursor.getInt(3));
            notify.setIfSound(cursor.getInt(4));
            notify.setTime(cursor.getString(5));
            notify.setCreat_time((cursor.getString(6)));
        }
        return notify;
    }
}