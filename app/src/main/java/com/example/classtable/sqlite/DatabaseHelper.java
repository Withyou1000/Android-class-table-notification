package com.example.classtable.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.classtable.bean.TableCell;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper Helper;
    private static SQLiteDatabase Write;
    private static SQLiteDatabase Read;
    private static String DB_NAME = "courses.db";
    private static String COURSE_INFO = "courses";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table courses(" +
                "id integer primary key autoincrement," +
                "_row integer," +
                "_col integer," +
                "course_name text," +
                "week text," +
                "teacher text," +
                "location text," +
                "note text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static DatabaseHelper getInstance(Context context) {
        if (Helper == null) {
            Helper = new DatabaseHelper(context);
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

    public List<TableCell> queryall() {
        String sql = "SELECT * FROM " + COURSE_INFO;
        List<TableCell> list = new ArrayList<>();
        Cursor cursor = Read.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            TableCell course = new TableCell();
            course.setCourseId(cursor.getInt(0));
            course.setRow(cursor.getInt(1));
            course.setCol(cursor.getInt(2));
            course.setName(cursor.getString(3));
            course.setTime(cursor.getString(4));
            course.setTeacher(cursor.getString(5));
            course.setLocation((cursor.getString(6)));
            course.setNote(cursor.getString(7));
            list.add(course);
        }
        return list;
    }

    public List<Integer> query_Day(int col) {
        String sql = "SELECT * FROM " + COURSE_INFO + " WHERE _col = ?";
        List<Integer> list = new ArrayList<>();
        Cursor cursor = Read.rawQuery(sql, new String[]{String.valueOf(col)});
        while (cursor.moveToNext()) {
            int row = cursor.getInt(1);
            list.add(row);
        }
        return list;
    }

    public boolean isEmpty() {
        Cursor cursor = Read.rawQuery("SELECT COUNT(*) FROM " + COURSE_INFO, null);
        cursor.moveToFirst();  // 移动到查询结果的第一行
        int count = cursor.getInt(0);  // 获取 COUNT(*) 的结果
        if (count == 0) {
            return true;
        } else {
            return false;
        }
    }

    public TableCell queryOne(int row, int col) {
        TableCell course = null;
        Cursor cursor = Read.query(COURSE_INFO, null, "_row=? AND _col=?", new String[]{String.valueOf(row), String.valueOf(col)}, null, null, null);
        if (cursor.moveToNext()) {
            course = new TableCell();
            course.setCourseId(cursor.getInt(0));
            course.setRow(cursor.getInt(1));
            course.setCol(cursor.getInt(2));
            course.setName(cursor.getString(3));
            course.setTime(cursor.getString(4));
            course.setTeacher(cursor.getString(5));
            course.setLocation((cursor.getString(6)));
            course.setNote(cursor.getString(7));

        }
        return course;
    }

    public void insert_course(TableCell course) {
        ContentValues values = new ContentValues();
        values.put("_row", course.getRow());
        values.put("_col", course.getCol());
        values.put("course_name", course.getName());
        values.put("week", course.getTime());
        values.put("teacher", course.getTeacher());
        values.put("location", course.getLocation());
        values.put("note", course.getNote());
        if (is_have_theCourse(course.getRow(), course.getCol())) {
            Write.update(COURSE_INFO, values, "_row=? AND _col=?", new String[]{String.valueOf(course.getRow()), String.valueOf(course.getCol())});
        } else {
            Write.insert(COURSE_INFO, null, values);
        }
    }

    private boolean is_have_theCourse(int row, int col) {
        TableCell course = null;
        Cursor cursor = Read.query(COURSE_INFO, null, "_row=? AND _col=?", new String[]{String.valueOf(row), String.valueOf(col)}, null, null, null);
        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }

    public void delete_course(int row, int col) {
        Write.delete(COURSE_INFO, "_row=? AND _col=?", new String[]{String.valueOf(row), String.valueOf(col)});
    }
}
